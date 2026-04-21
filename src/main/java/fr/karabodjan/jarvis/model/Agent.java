package fr.karabodjan.jarvis.model;

import java.time.Instant;

public class Agent {

    private final String id;
    private final String name;
    private final String repoUrl;
    private final String taskType;
    private final Instant createdAt;

    private String status;

    public Agent(String id, String name, String repoUrl, String taskType){
        this.id= id;
        this.name = name;
        this.repoUrl = repoUrl;
        this.taskType = taskType;
        this.createdAt = Instant.now();
        this.status = "IDLE";
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
