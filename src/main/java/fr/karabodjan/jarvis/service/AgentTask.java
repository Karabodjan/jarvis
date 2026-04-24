package fr.karabodjan.jarvis.service;

import fr.karabodjan.jarvis.model.Agent;
import fr.karabodjan.jarvis.model.run.AgentResult;
import javafx.concurrent.Task;

import java.time.Instant;
import java.util.Objects;

public abstract class AgentTask extends Task<AgentResult> {

    private final Agent agent;

    protected AgentTask(Agent agent) {
        this.agent = Objects.requireNonNull(agent, "agent must not be null");
    }

    /** The Agent that originated this run. Never null. */
    public final Agent getAgent() {
        return agent;
    }

    @Override
    protected final AgentResult call() throws Exception {
        Instant startedAt = Instant.now();
        updateMessage("Starting agent " + agent.getName() + "...");
        return doRun(startedAt);
    }

    protected abstract AgentResult doRun(Instant startedAt) throws Exception;
}