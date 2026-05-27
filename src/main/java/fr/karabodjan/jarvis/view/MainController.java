package fr.karabodjan.jarvis.view;

import fr.karabodjan.jarvis.model.run.AgentRunStatus;
import fr.karabodjan.jarvis.viewmodel.AgentListViewModel;
import fr.karabodjan.jarvis.viewmodel.AgentRunViewModel;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;


public class MainController {

    private static final DateTimeFormatter CREATED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
                    .withZone(ZoneId.systemDefault());

    @FXML private ListView<AgentRunViewModel> agentListView;

    @FXML private Label detailTitleLabel;
    @FXML private Label detailPlaceholderLabel;
    @FXML private VBox detailFieldsBox;
    @FXML private Label detailRepoLabel;
    @FXML private Label detailTaskTypeLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailCreatedAtLabel;
    @FXML private Label detailMessageLabel;
    @FXML private ProgressBar detailProgressBar;
    @FXML private Button launchButton;

    private AgentListViewModel listViewModel;

    private AgentRunViewModel boundRun;

    private final ChangeListener<AgentRunStatus> statusListener =
            (obs, oldStatus, newStatus) -> refreshLaunchButton(newStatus);

    public void setViewModel(AgentListViewModel listViewModel) {
        this.listViewModel = Objects.requireNonNull(listViewModel, "listViewModel must not be null");
    }

    public void init() {
        Objects.requireNonNull(listViewModel, "setViewModel must be called before init");

        agentListView.setItems(listViewModel.getAgents());

        agentListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(AgentRunViewModel runVm, boolean empty) {
                super.updateItem(runVm, empty);
                setText(empty || runVm == null ? null : runVm.getAgent().getName());
            }
        });

        agentListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldRun, newRun) -> bindToRun(newRun)
        );

        // Initial state: nothing selected.
        bindToRun(null);
    }

    // --- selection-driven binding --------------------------------------

    private void bindToRun(AgentRunViewModel runVm) {
        // Unbind the previously bound run, if any.
        if (boundRun != null) {
            detailMessageLabel.textProperty().unbind();
            detailProgressBar.progressProperty().unbind();
            detailStatusLabel.textProperty().unbind();
            boundRun.statusProperty().removeListener(statusListener);
            boundRun = null;
        }

        // Empty selection → show placeholder, hide details.
        if (runVm == null) {
            detailTitleLabel.setText("Select an agent");
            detailPlaceholderLabel.setVisible(true);
            detailPlaceholderLabel.setManaged(true);
            detailFieldsBox.setVisible(false);
            detailFieldsBox.setManaged(false);
            return;
        }

        // Populate static fields (from immutable Agent).
        var agent = runVm.getAgent();
        detailTitleLabel.setText(agent.getName());
        detailPlaceholderLabel.setVisible(false);
        detailPlaceholderLabel.setManaged(false);
        detailFieldsBox.setVisible(true);
        detailFieldsBox.setManaged(true);

        detailRepoLabel.setText(agent.getRepoUrl());
        detailTaskTypeLabel.setText(agent.getTaskType());
        detailCreatedAtLabel.setText(CREATED_AT_FORMAT.format(agent.getCreatedAt()));

        // Bind reactive fields to the run view model's properties.
        detailMessageLabel.textProperty().bind(runVm.messageProperty());
        detailProgressBar.progressProperty().bind(runVm.progressProperty());
        detailStatusLabel.textProperty().bind(runVm.statusProperty().asString());

        // Track the bound run and listen to status changes for the launch button.
        boundRun = runVm;
        runVm.statusProperty().addListener(statusListener);
        refreshLaunchButton(runVm.getStatus());
    }

    private void refreshLaunchButton(AgentRunStatus status) {
        // Disable Launch while RUNNING. Other states (IDLE/COMPLETED/FAILED/CANCELLED)
        // allow re-launching.
        launchButton.setDisable(status == AgentRunStatus.RUNNING);
    }

    // --- FXML handlers --------------------------------------------------

    @FXML
    private void onLaunchClicked() {
        if (boundRun == null) {
            return;  // no selection; button shouldn't even be clickable
        }
        listViewModel.launch(boundRun);
    }
}