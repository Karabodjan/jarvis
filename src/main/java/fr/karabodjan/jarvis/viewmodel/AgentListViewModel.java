package fr.karabodjan.jarvis.viewmodel;

import fr.karabodjan.jarvis.model.Agent;
import fr.karabodjan.jarvis.model.run.AgentRunStatus;
import fr.karabodjan.jarvis.service.AgentTask;
import fr.karabodjan.jarvis.service.IAgentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public final class AgentListViewModel {

    private static final int MAX_LOG_LINES = 500;

    private static final DateTimeFormatter LOG_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final IAgentService agentService;

    private final ObservableList<AgentRunViewModel> agents =
            FXCollections.observableArrayList();

    private final ObservableList<String> logs =
            FXCollections.observableArrayList();


    private final Map<String, AgentTask> runningTasks = new HashMap<>();

    public AgentListViewModel(IAgentService agentService) {
        this.agentService = Objects.requireNonNull(agentService, "agentService must not be null");
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
        task.setOnFailed(event -> handleFailed(runVm, task));
        task.setOnCancelled(event -> handleCancelled(runVm, task));

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
    }

    private void handleFailed(AgentRunViewModel runVm, AgentTask task) {
        Throwable error = task.getException();
        String errorMessage = error != null ? error.getMessage() : "Unknown error";

        runVm.setStatus(AgentRunStatus.FAILED);
        runVm.setMessage("Failed: " + errorMessage);
        runVm.setLastResult(null);
        runningTasks.remove(runVm.getAgent().getId());

        appendLog(runVm.getAgent(), "✖ Failed — " + errorMessage);
    }

    private void handleCancelled(AgentRunViewModel runVm, AgentTask task) {
        runVm.setStatus(AgentRunStatus.CANCELLED);
        runVm.setMessage("Cancelled by user.");
        runVm.setLastResult(null);
        runningTasks.remove(runVm.getAgent().getId());

        appendLog(runVm.getAgent(), "⏹ Cancelled by user");
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