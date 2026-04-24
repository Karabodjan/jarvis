package fr.karabodjan.jarvis.service;

import fr.karabodjan.jarvis.model.Agent;

import java.time.Duration;
import java.util.Objects;

public final class MockAgentService implements IAgentService {

    /** Default simulated duration of a mock run. Short enough for dev loop speed. */
    public static final Duration DEFAULT_DURATION = Duration.ofSeconds(8);

    private final Duration simulatedDuration;


    public MockAgentService() {
        this(DEFAULT_DURATION);
    }

    public MockAgentService(Duration simulatedDuration) {
        this.simulatedDuration = Objects.requireNonNull(simulatedDuration, "simulatedDuration must not be null");
        if (simulatedDuration.isNegative() || simulatedDuration.isZero()) {
            throw new IllegalArgumentException("simulatedDuration must be strictly positive");
        }
    }

    @Override
    public AgentTask launch(Agent agent) {
        Objects.requireNonNull(agent, "agent must not be null");
        return new MockAgentTask(agent, simulatedDuration);
    }

    // Referenced in Javadoc above; keeps the value in one place.
    @SuppressWarnings("unused")
    private static final long DEFAULT_DURATION_SECONDS = DEFAULT_DURATION.toSeconds();
}