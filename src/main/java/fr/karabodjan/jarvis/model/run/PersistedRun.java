package fr.karabodjan.jarvis.model.run;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record PersistedRun(
        String runId,
        String agentId,
        String agentName,
        AgentRunStatus status,
        Instant startedAt,
        Instant completedAt,
        String prUrl,          // nullable
        String errorMessage,   // nullable
        boolean merged
) {
    public PersistedRun {
        Objects.requireNonNull(runId, "runId");
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(agentName, "agentName");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(completedAt, "completedAt");
    }

    public Duration duration() {
        return Duration.between(startedAt, completedAt);
    }
}