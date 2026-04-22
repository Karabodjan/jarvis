package fr.karabodjan.jarvis.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.karabodjan.jarvis.model.Agent;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class JsonLoader {

    private static final String AGENTS_RESOURCE_PATH = "/agents.json";

    private final ObjectMapper mapper;

    public JsonLoader() {
        this.mapper = new ObjectMapper();
    }

    public List<Agent> loadAgents() {
        try (InputStream input = getClass().getResourceAsStream(AGENTS_RESOURCE_PATH)) {

            if (input == null) {
                throw new JarvisConfigException(
                        "Resource not found: " + AGENTS_RESOURCE_PATH, null);
            }

            Map<String, List<Agent>> root = mapper.readValue(
                    input,
                    new TypeReference<>() {}
            );

            List<Agent> agents = root.get("agents");
            if (agents == null) {
                throw new JarvisConfigException(
                        "Missing 'agents' key in " + AGENTS_RESOURCE_PATH, null);
            }

            return agents;

        } catch (IOException e) {
            throw new JarvisConfigException(
                    "Failed to parse " + AGENTS_RESOURCE_PATH + ": " + e.getMessage(), e);
        }
    }
}