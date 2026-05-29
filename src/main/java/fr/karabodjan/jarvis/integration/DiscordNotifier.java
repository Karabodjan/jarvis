package fr.karabodjan.jarvis.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.karabodjan.jarvis.model.run.AgentRunStatus;
import fr.karabodjan.jarvis.model.run.PersistedRun;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscordNotifier {

    private static final int COLOR_GREEN  = 3066993;   // ✅ COMPLETED
    private static final int COLOR_RED    = 15158332;  // ❌ FAILED
    private static final int COLOR_YELLOW = 16776960;  // ⏳ RUNNING
    private static final int COLOR_BLUE   = 3447003;   // 🔀 MERGED

    private final String webhookUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "jarvis-discord");
        t.setDaemon(true);
        return t;
    });

    public DiscordNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.mapper     = new ObjectMapper();
    }

    public void send(PersistedRun run) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            System.err.println("[Discord] Webhook URL not configured — skipping.");
            return;
        }

        executor.submit(() -> {
            try {
                String body = buildPayload(run);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString());

                // (5) Discord devolve 204 No Content em caso de sucesso
                if (response.statusCode() != 204) {
                    System.err.println("[Discord] Unexpected status: "
                            + response.statusCode() + " — " + response.body());
                } else {
                    System.out.println("[Discord] Notification sent for run "
                            + run.runId());
                }

            } catch (Exception e) {
                // (6) Discord é não-crítico — nunca derruba a app
                System.err.println("[Discord] Failed to send notification: "
                        + e.getMessage());
            }
        });
    }

    private String buildPayload(PersistedRun run) throws Exception {
        int color = colorFor(run.status());
        String title = titleFor(run.status());
        String duration = formatDuration(run);

        List<Map<String, Object>> fields = new java.util.ArrayList<>();
        fields.add(field("Agent",    run.agentName(), true));
        fields.add(field("Status",   run.status().name(), true));
        fields.add(field("Duration", duration, true));

        if (run.prUrl() != null && !run.prUrl().isBlank()) {
            fields.add(field("Pull Request", run.prUrl(), false));
        }
        if (run.errorMessage() != null && !run.errorMessage().isBlank()) {
            fields.add(field("Error", run.errorMessage(), false));
        }

        Map<String, Object> embed = Map.of(
                "title",  title,
                "color",  color,
                "fields", fields
        );

        Map<String, Object> payload = Map.of("embeds", List.of(embed));

        return mapper.writeValueAsString(payload);
    }

    // --- helpers --------------------------------------------------------

    private Map<String, Object> field(String name, String value, boolean inline) {
        return Map.of("name", name, "value", value, "inline", inline);
    }

    private int colorFor(AgentRunStatus status) {
        return switch (status) {
            case COMPLETED -> COLOR_GREEN;
            case FAILED    -> COLOR_RED;
            case RUNNING   -> COLOR_YELLOW;
            default        -> COLOR_BLUE;
        };
    }

    private String titleFor(AgentRunStatus status) {
        return switch (status) {
            case COMPLETED -> "✅ JARVIS — Agent Completed";
            case FAILED    -> "❌ JARVIS — Agent Failed";
            case CANCELLED -> "⏹ JARVIS — Agent Cancelled";
            default        -> "ℹ️ JARVIS — Agent Update";
        };
    }

    private String formatDuration(PersistedRun run) {
        if (run.startedAt() == null || run.completedAt() == null) {
            return "N/A";
        }
        long seconds = run.duration().toSeconds();
        long minutes = seconds / 60;
        long secs    = seconds % 60;
        return minutes > 0
                ? minutes + "m " + secs + "s"
                : secs + "s";
    }

    public void shutdown() {
        executor.shutdownNow();
    }


}