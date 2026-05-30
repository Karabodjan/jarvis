package fr.karabodjan.jarvis.model;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ce fichier représente la configuration d'un agent d'IA.
 * L'agent a récupéré le fichier agents.json.
 * Ne stocke pas les états (IDLE, RUNNING, COMPLETED)
 */
public final class Agent {

    private final String id;
    private final String name;
    private final String repoUrl;
    private final String taskType;
    private final Instant createdAt;

    // Desseriazation
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

    //Getters
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

    // Définit la manière dont l'objet sera affiché à l'impression.
    @Override
    public String toString() {
        return "Agent{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", taskType='" + taskType + '\'' +
                '}';
    }
}