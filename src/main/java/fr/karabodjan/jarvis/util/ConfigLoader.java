package fr.karabodjan.jarvis.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class ConfigLoader {

    private static final String CONFIG_PATH = "/config.json";

    private final ObjectMapper mapper = new ObjectMapper();

    public JarvisConfig load() {
        InputStream stream = getClass().getResourceAsStream(CONFIG_PATH);

        if (stream == null) {
            System.err.println("[JARVIS] config.json not found — " +
                    "Discord, GitHub and auto-merge will be disabled.");
            return new JarvisConfig();
        }

        try (stream) {
            JarvisConfig config = mapper.readValue(stream, JarvisConfig.class);
            System.out.println("[JARVIS] config.json loaded.");
            return config;
        } catch (Exception e) {
            System.err.println("[JARVIS] Failed to parse config.json: "
                    + e.getMessage());
            return new JarvisConfig();
        }
    }
}