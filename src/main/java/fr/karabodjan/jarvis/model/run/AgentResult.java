package fr.karabodjan.jarvis.model.run;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record AgentResult(
        String agentId,
        AgentRunStatus status,
        String prUrl,
        String summary,
        Instant startedAt,
        Instant completedAt,
        String errorMessage
) {

    public AgentResult {
        Objects.requireNonNull(agentId, "agentId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        Objects.requireNonNull(completedAt, "completedAt must not be null");

        if (!status.isTerminal()) {
            throw new IllegalArgumentException(
                    "AgentResult requires a terminal status, got: " + status);
        }
        if (completedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException(
                    "completedAt (" + completedAt + ") must not be before startedAt (" + startedAt + ")");
        }
        if (status == AgentRunStatus.COMPLETED && prUrl == null) {
            throw new IllegalArgumentException(
                    "COMPLETED result must have a non-null prUrl");
        }
        if (status != AgentRunStatus.COMPLETED && prUrl != null) {
            throw new IllegalArgumentException(
                    "Only COMPLETED results may carry a prUrl; got status=" + status);
        }
        if (status == AgentRunStatus.FAILED && errorMessage == null) {
            throw new IllegalArgumentException(
                    "FAILED result must have a non-null errorMessage");
        }
        if (status != AgentRunStatus.FAILED && errorMessage != null) {
            throw new IllegalArgumentException(
                    "Only FAILED results may carry an errorMessage; got status=" + status);
        }
    }

    /** Convenience accessor: the duration of the run. Never null. */
    public Duration duration() {
        return Duration.between(startedAt, completedAt);
    }
}