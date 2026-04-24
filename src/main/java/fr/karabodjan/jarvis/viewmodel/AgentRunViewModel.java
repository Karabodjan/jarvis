package fr.karabodjan.jarvis.viewmodel;

import fr.karabodjan.jarvis.model.Agent;
import fr.karabodjan.jarvis.model.run.AgentResult;
import fr.karabodjan.jarvis.model.run.AgentRunStatus;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public final class AgentRunViewModel {

    private final Agent agent;

    private final ObjectProperty<AgentRunStatus> status =
            new SimpleObjectProperty<>(this, "status", AgentRunStatus.IDLE);

    private final StringProperty message =
            new SimpleStringProperty(this, "message", "");

    private final DoubleProperty progress =
            new SimpleDoubleProperty(this, "progress", 0.0);

    private final ObjectProperty<AgentResult> lastResult =
            new SimpleObjectProperty<>(this, "lastResult", null);

    public AgentRunViewModel(Agent agent) {
        this.agent = Objects.requireNonNull(agent, "agent must not be null");
    }

    /** The immutable agent definition wrapped by this ViewModel. */
    public Agent getAgent() {
        return agent;
    }

    // --- status ---------------------------------------------------------

    public AgentRunStatus getStatus() {
        return status.get();
    }

    /** Package-private setter — only the ViewModel layer mutates state. */
    void setStatus(AgentRunStatus newStatus) {
        this.status.set(Objects.requireNonNull(newStatus, "status must not be null"));
    }

    /** Read-only property for View binding. */
    public ReadOnlyObjectProperty<AgentRunStatus> statusProperty() {
        return status;
    }

    // --- message --------------------------------------------------------

    public String getMessage() {
        return message.get();
    }

    void setMessage(String newMessage) {
        this.message.set(newMessage == null ? "" : newMessage);
    }

    public ReadOnlyStringProperty messageProperty() {
        return message;
    }

    // --- progress -------------------------------------------------------

    public double getProgress() {
        return progress.get();
    }

    void setProgress(double newProgress) {
        this.progress.set(newProgress);
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }

    // --- lastResult -----------------------------------------------------

    public AgentResult getLastResult() {
        return lastResult.get();
    }

    void setLastResult(AgentResult newResult) {
        this.lastResult.set(newResult);
    }

    public ReadOnlyObjectProperty<AgentResult> lastResultProperty() {
        return lastResult;
    }

    // --- convenience ----------------------------------------------------

    public boolean isRunning() {
        return status.get() == AgentRunStatus.RUNNING;
    }

    @Override
    public String toString() {
        return "AgentRunViewModel{" +
                "agent=" + agent.getId() +
                ", status=" + status.get() +
                '}';
    }
}