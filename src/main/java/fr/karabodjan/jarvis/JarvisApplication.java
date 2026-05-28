package fr.karabodjan.jarvis;

import fr.karabodjan.jarvis.model.Agent;
import fr.karabodjan.jarvis.repository.RunHistoryRepository;
import fr.karabodjan.jarvis.repository.SqliteRunRepository;
import fr.karabodjan.jarvis.service.IAgentService;
import fr.karabodjan.jarvis.service.MockAgentService;
import fr.karabodjan.jarvis.util.JarvisConfigException;
import fr.karabodjan.jarvis.util.JsonLoader;
import fr.karabodjan.jarvis.view.MainController;
import fr.karabodjan.jarvis.viewmodel.AgentListViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class JarvisApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load agent definitions from JSON.
        List<Agent> agents = loadAgentsSafely();

        IAgentService agentService = new MockAgentService();

        // Persistence: SQLite repository (cria jarvis.db + schema no 1º arranque).
        RunHistoryRepository runHistoryRepository = new SqliteRunRepository("jarvis.db");

        AgentListViewModel listViewModel =
                new AgentListViewModel(agentService, runHistoryRepository);
        listViewModel.setAgents(agents);

        // Load the FXML and inject the ViewModel into the controller.
        FXMLLoader fxmlLoader = new FXMLLoader(JarvisApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 650);
        MainController controller = fxmlLoader.getController();
        controller.setViewModel(listViewModel);
        controller.init();

        stage.setTitle("J.A.R.V.I.S. — Control Tower");
        stage.setScene(scene);
        stage.show();
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