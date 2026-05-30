package fr.karabodjan.jarvis.service;

import fr.karabodjan.jarvis.model.Agent;
import fr.karabodjan.jarvis.model.run.AgentResult;
import fr.karabodjan.jarvis.model.run.AgentRunStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class MockAgentTask extends AgentTask {

    private static final List<String> PHASES = List.of(
            "Analysing repository",
            "Planning changes",
            "Applying code modifications",
            "Running tests",
            "Submitting pull request"
    );

    private final Duration totalDuration;

    MockAgentTask(Agent agent, Duration totalDuration) {
        super(agent);
        this.totalDuration = Objects.requireNonNull(totalDuration, "totalDuration must not be null");
        if (totalDuration.isNegative() || totalDuration.isZero()) {
            throw new IllegalArgumentException("totalDuration must be strictly positive");
        }
    }

    @Override
    protected AgentResult doRun(Instant startedAt) throws Exception {
        long phaseMillis = totalDuration.toMillis() / PHASES.size();

        for (int i = 0; i < PHASES.size(); i++) {
            if (isCancelled()) {
                throw new InterruptedException("Cancelled before phase " + i);
            }

            updateMessage(PHASES.get(i) + "...");
            updateProgress(i, PHASES.size());

            Thread.sleep(phaseMillis);  // throws InterruptedException if cancelled
        }

        updateMessage("Done.");
        updateProgress(PHASES.size(), PHASES.size());

        Instant completedAt = Instant.now();
        String prUrl = buildFakePrUrl(getAgent());
        String summary = "Mock run completed successfully (simulated "
                + PHASES.size() + " phases over " + totalDuration.toSeconds() + "s).";

        return new AgentResult(
                getAgent().getId(),
                AgentRunStatus.COMPLETED,
                prUrl,
                summary,
                startedAt,
                completedAt,
                null
        );
    }

    private static String buildFakePrUrl(Agent agent) {
        // URL real para testar o auto-merge com o jarvis-test-repo-1
        return "https://github.com/Karabodjan/jarvis-test-repo-1/pull/1";
    }
}