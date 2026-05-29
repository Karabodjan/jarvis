package fr.karabodjan.jarvis.util;

public class JarvisConfig {

    private String discordWebhookUrl;
    private String githubToken;
    private boolean autoMergeEnabled;

    public JarvisConfig() {}

    public String getDiscordWebhookUrl() { return discordWebhookUrl; }
    public void setDiscordWebhookUrl(String discordWebhookUrl) {
        this.discordWebhookUrl = discordWebhookUrl;
    }

    public String getGithubToken() { return githubToken; }
    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }

    public boolean isAutoMergeEnabled() { return autoMergeEnabled; }
    public void setAutoMergeEnabled(boolean autoMergeEnabled) {
        this.autoMergeEnabled = autoMergeEnabled;
    }
}