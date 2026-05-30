package fr.karabodjan.jarvis.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GitHubService {

    private final String token;
    private final HttpClient httpClient;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "jarvis-github");
        t.setDaemon(true);
        return t;
    });

    public GitHubService(String token) {
        this.token      = token;
        this.httpClient = HttpClient.newHttpClient();
    }

    @FunctionalInterface
    public interface MergeCallback {
        void onResult(boolean success, String message);
    }

    public void mergePullRequest(String prUrl, MergeCallback callback) {
        if (token == null || token.isBlank()) {
            System.err.println("[GitHub] Token not configured — skipping merge.");
            callback.onResult(false, "GitHub token not configured.");
            return;
        }
        if (prUrl == null || prUrl.isBlank()) {
            System.err.println("[GitHub] No PR URL — skipping merge.");
            callback.onResult(false, "No PR URL.");
            return;
        }

        executor.submit(() -> {
            try {
                String[] parts = prUrl.replace("https://github.com/", "").split("/");
                if (parts.length < 4) {
                    callback.onResult(false, "Invalid PR URL: " + prUrl);
                    return;
                }
                String owner    = parts[0];
                String repo     = parts[1];
                String number   = parts[3]; // índice 2 é "pull"

                String apiUrl = "https://api.github.com/repos/"
                        + owner + "/" + repo + "/pulls/" + number + "/merge";

                String body = "{\"merge_method\":\"merge\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .timeout(Duration.ofSeconds(15))
                        .header("Authorization", "Bearer " + token)
                        .header("Accept", "application/vnd.github+json")
                        .header("Content-Type", "application/json")
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .PUT(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    System.out.println("[GitHub] PR #" + number + " merged successfully.");
                    callback.onResult(true, "PR #" + number + " merged.");
                } else {
                    System.err.println("[GitHub] Merge failed: "
                            + response.statusCode() + " — " + response.body());
                    callback.onResult(false, "Merge failed: HTTP " + response.statusCode());
                }

            } catch (Exception e) {
                System.err.println("[GitHub] Exception during merge: " + e.getMessage());
                callback.onResult(false, e.getMessage());
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}