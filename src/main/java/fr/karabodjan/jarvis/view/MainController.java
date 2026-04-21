package fr.karabodjan.jarvis.view;

import fr.karabodjan.jarvis.model.Agent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private ListView<Agent> agentListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Agent> placeholder = FXCollections.observableArrayList(
                new Agent("a1", "Placeholder Agent 1", "https://github.com/x/y", "bug_fix"),
                new Agent("a2", "Placeholder Agent 2", "https://github.com/x/z", "refactor")
        );
        agentListView.setItems(placeholder);

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
}