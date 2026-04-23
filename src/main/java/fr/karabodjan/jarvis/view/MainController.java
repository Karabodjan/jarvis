package fr.karabodjan.jarvis.view;

import fr.karabodjan.jarvis.model.Agent;
import fr.karabodjan.jarvis.util.JarvisConfigException;
import fr.karabodjan.jarvis.util.JsonLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final DateTimeFormatter CREATED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
                    .withZone(ZoneId.systemDefault());

    @FXML private ListView<Agent> agentListView;

    @FXML private Label detailTitleLabel;
    @FXML private Label detailPlaceholderLabel;
    @FXML private VBox detailFieldsBox;
    @FXML private Label detailRepoLabel;
    @FXML private Label detailTaskTypeLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailCreatedAtLabel;

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

        agentListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldAgent, newAgent) -> showAgentDetails(newAgent)
        );
    }

    private void showAgentDetails(Agent agent) {
        if (agent == null) {
            detailTitleLabel.setText("Select an agent");
            detailPlaceholderLabel.setVisible(true);
            detailPlaceholderLabel.setManaged(true);
            detailFieldsBox.setVisible(false);
            detailFieldsBox.setManaged(false);
            return;
        }

        detailTitleLabel.setText(agent.getName());
        detailPlaceholderLabel.setVisible(false);
        detailPlaceholderLabel.setManaged(false);
        detailFieldsBox.setVisible(true);
        detailFieldsBox.setManaged(true);

        detailRepoLabel.setText(agent.getRepoUrl());
        detailTaskTypeLabel.setText(agent.getTaskType());
        detailStatusLabel.setText(agent.getStatus());
        detailCreatedAtLabel.setText(CREATED_AT_FORMAT.format(agent.getCreatedAt()));
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