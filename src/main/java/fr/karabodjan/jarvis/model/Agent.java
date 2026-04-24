package fr.karabodjan.jarvis.model;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable definition of an agent loaded from agents.json.
 * Represents the static configuration of an agent — never its runtime state.
 * Runtime status (IDLE, RUNNING, COMPLETED, ...) is managed by the ViewModel.
 */
public final class Agent {

    private final String id;
    private final String name;
    private final String repoUrl;
    private final String taskType;
    private final Instant createdAt;

    public Agent(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("repoUrl") String repoUrl,
            @JsonProperty("taskType") String taskType) {
        this.id = id;
        this.name = name;
        this.repoUrl = repoUrl;
        this.taskType = taskType;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public String getTaskType() {
        return taskType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", taskType='" + taskType + '\'' +
                '}';
    }
}