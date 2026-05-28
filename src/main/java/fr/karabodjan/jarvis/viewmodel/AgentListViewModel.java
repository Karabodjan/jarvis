package fr.karabodjan.jarvis.viewmodel;

import fr.karabodjan.jarvis.model.Agent;
import fr.karabodjan.jarvis.model.run.AgentResult;
import fr.karabodjan.jarvis.model.run.AgentRunStatus;
import fr.karabodjan.jarvis.model.run.PersistedRun;
import fr.karabodjan.jarvis.repository.JarvisStorageException;
import fr.karabodjan.jarvis.repository.RunHistoryRepository;
import fr.karabodjan.jarvis.service.AgentTask;
import fr.karabodjan.jarvis.service.IAgentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public final class AgentListViewModel {

    private static final int MAX_LOG_LINES = 500;

    private static final DateTimeFormatter LOG_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final IAgentService agentService;
    private final RunHistoryRepository runHistoryRepository;

    private final ObservableList<AgentRunViewModel> agents =
            FXCollections.observableArrayList();

    private final ObservableList<String> logs =
            FXCollections.observableArrayList();

    private final Map<String, AgentTask> runningTasks = new HashMap<>();

    // Single-thread daemon executor: persistência assíncrona E serializada
    // (não bloqueia a FX thread, e evita escritas SQLite concorrentes).
    private final ExecutorService persistExecutor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "jarvis-persist");
                t.setDaemon(true);
                return t;
            });

    public AgentListViewModel(IAgentService agentService,
                              RunHistoryRepository runHistoryRepository) {
        this.agentService = Objects.requireNonNull(agentService, "agentService must not be null");
        this.runHistoryRepository = Objects.requireNonNull(runHistoryRepository, "runHistoryRepository must not be null");
    }

    // --- loading --------------------------------------------------------

    public void setAgents(List<Agent> loadedAgents) {
        Objects.requireNonNull(loadedAgents, "loadedAgents must not be null");
        agents.clear();
        for (Agent agent : loadedAgents) {
            agents.add(new AgentRunViewModel(agent));
        }
    }

    public ObservableList<AgentRunViewModel> getAgents() {
        return agents;
    }

    public ObservableList<String> getLogs() {
        return logs;
    }

    // --- launch / cancel -----------------------------------------------

    public void launch(AgentRunViewModel runVm) {
        Objects.requireNonNull(runVm, "runVm must not be null");
        if (runVm.isRunning()) {
            return;
        }

        Agent agent = runVm.getAgent();
        AgentTask task = agentService.launch(agent);

        // Momento aproximado de início — usado nas runs que falham ou são
        // canceladas (nesses casos não há AgentResult com startedAt).
        Instant launchedAt = Instant.now();

        runVm.setStatus(AgentRunStatus.RUNNING);
        runVm.setMessage("Launching...");
        runVm.setProgress(0.0);

        appendLog(agent, "▶ Launching agent");

        task.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            runVm.setMessage(newMsg);
            // Skip empty messages (initial state) to avoid noise.
            if (newMsg != null && !newMsg.isBlank()) {
                appendLog(agent, newMsg);
            }
        });
        task.progressProperty().addListener((obs, oldP, newP) -> runVm.setProgress(newP.doubleValue()));

        task.setOnSucceeded(event -> handleSucceeded(runVm, task));
        task.setOnFailed(event -> handleFailed(runVm, task, launchedAt));
        task.setOnCancelled(event -> handleCancelled(runVm, launchedAt));

        runningTasks.put(agent.getId(), task);

        Thread thread = new Thread(task, "agent-run-" + agent.getId());
        thread.setDaemon(true);
        thread.start();
    }

    public void cancel(AgentRunViewModel runVm) {
        Objects.requireNonNull(runVm, "runVm must not be null");
        AgentTask task = runningTasks.get(runVm.getAgent().getId());
        if (task != null) {
            task.cancel();
        }
    }

    // --- callbacks ------------------------------------------------------

    private void handleSucceeded(AgentRunViewModel runVm, AgentTask task) {
        var result = task.getValue();
        runVm.setStatus(result.status());
        runVm.setMessage(result.summary());
        runVm.setProgress(1.0);
        runVm.setLastResult(result);
        runningTasks.remove(runVm.getAgent().getId());

        appendLog(runVm.getAgent(), "✔ Completed — " + result.summary());
        if (result.prUrl() != null) {
            appendLog(runVm.getAgent(), "  PR: " + result.prUrl());
        }

        persistAsync(toPersistedRun(runVm.getAgent(), result));
    }

    private void handleFailed(AgentRunViewModel runVm, AgentTask task, Instant startedAt) {
        Throwable error = task.getException();
        String errorMessage = error != null ? error.getMessage() : "Unknown error";

        runVm.setStatus(AgentRunStatus.FAILED);
        runVm.setMessage("Failed: " + errorMessage);
        runVm.setLastResult(null);
        runningTasks.remove(runVm.getAgent().getId());

        appendLog(runVm.getAgent(), "✖ Failed — " + errorMessage);

        persistAsync(new PersistedRun(
                UUID.randomUUID().toString(),
                runVm.getAgent().getId(),
                runVm.getAgent().getName(),
                AgentRunStatus.FAILED,
                startedAt,
                Instant.now(),
                null,           // prUrl
                errorMessage,
                false           // merged
        ));
    }

    private void handleCancelled(AgentRunViewModel runVm, Instant startedAt) {
        runVm.setStatus(AgentRunStatus.CANCELLED);
        runVm.setMessage("Cancelled by user.");
        runVm.setLastResult(null);
        runningTasks.remove(runVm.getAgent().getId());

        appendLog(runVm.getAgent(), "⏹ Cancelled by user");

        persistAsync(new PersistedRun(
                UUID.randomUUID().toString(),
                runVm.getAgent().getId(),
                runVm.getAgent().getName(),
                AgentRunStatus.CANCELLED,
                startedAt,
                Instant.now(),
                null,           // prUrl
                null,           // errorMessage
                false           // merged
        ));
    }

    // --- persistence ----------------------------------------------------

    private PersistedRun toPersistedRun(Agent agent, AgentResult result) {
        return new PersistedRun(
                UUID.randomUUID().toString(),
                agent.getId(),
                agent.getName(),
                result.status(),
                result.startedAt(),
                result.completedAt(),
                result.prUrl(),
                result.errorMessage(),
                false           // merged: sempre false até à Fase 5 (auto-merge)
        );
    }

    private void persistAsync(PersistedRun run) {
        persistExecutor.submit(() -> {
            try {
                runHistoryRepository.saveRun(run);
            } catch (JarvisStorageException e) {
                System.err.println("[JARVIS] Persist failed for run "
                        + run.runId() + ": " + e.getMessage());
            }
        });
    }

    // --- logs -----------------------------------------------------------

    private void appendLog(Agent agent, String message) {
        String line = "[" + LocalTime.now().format(LOG_TIME_FORMAT) + "] "
                + "[" + agent.getName() + "] "
                + message;
        logs.add(line);
        if (logs.size() > MAX_LOG_LINES) {
            logs.remove(0);
        }
    }

    // --- diagnostics ----------------------------------------------------

    public int getRunningCount() {
        return runningTasks.size();
    }
}