package fr.karabodjan.jarvis.view;

import fr.karabodjan.jarvis.model.Agent;
import fr.karabodjan.jarvis.util.JarvisConfigException;
import fr.karabodjan.jarvis.util.JsonLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private ListView<Agent> agentListView;

    private final JsonLoader jsonLoader = new JsonLoader();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Agent> agents = loadAgentsSafely();
        agentListView.setItems(agents);

        agentListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Agent agent, boolean empty) {
                super.updateItem(agent, empty);
                if (empty || agent == null) {
                    setText(null);
                } else {
                    setText(agent.getName());
                }
            }
        });
    }

    private ObservableList<Agent> loadAgentsSafely() {
        try {
            List<Agent> agents = jsonLoader.loadAgents();
            System.out.println("[JARVIS] Loaded " + agents.size() + " agents from agents.json");
            return FXCollections.observableArrayList(agents);
        } catch (JarvisConfigException e) {
            System.err.println("[JARVIS] Failed to load agents: " + e.getMessage());
            return FXCollections.observableArrayList(Collections.emptyList());
        }
    }
}