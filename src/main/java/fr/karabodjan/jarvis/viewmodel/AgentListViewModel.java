package fr.karabodjan.jarvis.viewmodel;

import fr.karabodjan.jarvis.model.Agent;
import fr.karabodjan.jarvis.model.run.AgentRunStatus;
import fr.karabodjan.jarvis.service.AgentTask;
import fr.karabodjan.jarvis.service.IAgentService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AgentListViewModel {

    private final IAgentService agentService;

    private final ObservableList<AgentRunViewModel> agents =
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

    public void launch(AgentRunViewModel runVm) {
        Objects.requireNonNull(runVm, "runVm must not be null");
        if (runVm.isRunning()) {
            return;
        }

        Agent agent = runVm.getAgent();
        AgentTask task = agentService.launch(agent);

        // Reset the run slot to a clean RUNNING state.
        runVm.setStatus(AgentRunStatus.RUNNING);
        runVm.setMessage("Launching...");
        runVm.setProgress(0.0);

        // the run view model directly.
        task.messageProperty().addListener((obs, oldMsg, newMsg) -> runVm.setMessage(newMsg));
        task.progressProperty().addListener((obs, oldP, newP) -> runVm.setProgress(newP.doubleValue()));

        task.setOnSucceeded(event -> handleSucceeded(runVm, task));
        task.setOnFailed(event -> handleFailed(runVm, task));
        task.setOnCancelled(event -> handleCancelled(runVm, task));

        runningTasks.put(agent.getId(), task);

        // Start the background thread AFTER listeners are registered —

        Thread thread = new Thread(task, "agent-run-" + agent.getId());
        thread.setDaemon(true);  // JVM can exit without waiting for in-flight runs
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
    }

    private void handleFailed(AgentRunViewModel runVm, AgentTask task) {
        Throwable error = task.getException();
        String errorMessage = error != null ? error.getMessage() : "Unknown error";

        runVm.setStatus(AgentRunStatus.FAILED);
        runVm.setMessage("Failed: " + errorMessage);

        runVm.setLastResult(null);
        runningTasks.remove(runVm.getAgent().getId());
    }

    private void handleCancelled(AgentRunViewModel runVm, AgentTask task) {
        runVm.setStatus(AgentRunStatus.CANCELLED);
        runVm.setMessage("Cancelled by user.");
        runVm.setLastResult(null);
        runningTasks.remove(runVm.getAgent().getId());
    }

    // --- diagnostics ---------------------------------------------------

    /** Number of runs currently in progress. For tests and debugging. */
    public int getRunningCount() {
        return runningTasks.size();
    }
}