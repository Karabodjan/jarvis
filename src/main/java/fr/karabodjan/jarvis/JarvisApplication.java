package fr.karabodjan.jarvis;

import fr.karabodjan.jarvis.integration.VoiceService;
import fr.karabodjan.jarvis.model.Agent;
import fr.karabodjan.jarvis.repository.RunHistoryRepository;
import fr.karabodjan.jarvis.repository.SqliteRunRepository;
import fr.karabodjan.jarvis.service.IAgentService;
import fr.karabodjan.jarvis.service.MockAgentService;
import fr.karabodjan.jarvis.util.ConfigLoader;
import fr.karabodjan.jarvis.util.JarvisConfig;
import fr.karabodjan.jarvis.util.JarvisConfigException;
import fr.karabodjan.jarvis.util.JsonLoader;
import fr.karabodjan.jarvis.view.MainController;
import fr.karabodjan.jarvis.viewmodel.AgentListViewModel;
import fr.karabodjan.jarvis.viewmodel.RunHistoryViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import fr.karabodjan.jarvis.integration.DiscordNotifier;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class JarvisApplication extends Application {

    private VoiceService voiceService;
    private DiscordNotifier discordNotifier;

    @Override
    public void start(Stage stage) throws IOException {
        List<Agent> agents = loadAgentsSafely();

        JarvisConfig config = new ConfigLoader().load();

        IAgentService agentService = new MockAgentService();
        RunHistoryRepository runHistoryRepository = new SqliteRunRepository("jarvis.db");
        voiceService = new VoiceService();
        discordNotifier = new DiscordNotifier(config.getDiscordWebhookUrl());

        AgentListViewModel listViewModel =
                new AgentListViewModel(agentService, runHistoryRepository, voiceService, discordNotifier);
        listViewModel.setAgents(agents);

        RunHistoryViewModel runHistoryViewModel =
                new RunHistoryViewModel(runHistoryRepository);

        FXMLLoader fxmlLoader = new FXMLLoader(
                JarvisApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 650);
        MainController controller = fxmlLoader.getController();
        controller.setViewModel(listViewModel);
        controller.setRunHistoryViewModel(runHistoryViewModel);
        controller.init();

        stage.setTitle("J.A.R.V.I.S. — Control Tower");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (voiceService != null)       voiceService.shutdown();
        if (discordNotifier != null)    discordNotifier.shutdown();
    }

    private List<Agent> loadAgentsSafely() {
        try {
            JsonLoader jsonLoader = new JsonLoader();
            List<Agent> agents = jsonLoader.loadAgents();
            System.out.println("[JARVIS] Loaded " + agents.size() + " agents from agents.json");
            return agents;
        } catch (JarvisConfigException e) {
            System.err.println("[JARVIS] Failed to load agents: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}