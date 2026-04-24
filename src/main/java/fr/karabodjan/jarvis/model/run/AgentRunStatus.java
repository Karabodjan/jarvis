package fr.karabodjan.jarvis.model.run;

public enum AgentRunStatus {

    /** No run in progress. Initial state and state after a run finishes. */
    IDLE,

    /** Run is currently executing in a background thread. */
    RUNNING,

    /** Run finished successfully (Pull Request was created). */
    COMPLETED,

    /** Run finished with an error (network failure, API error, exception). */
    FAILED,

    /** Run was deliberately cancelled by the user before completion. */
    CANCELLED
}