package fr.karabodjan.jarvis.view;

import fr.karabodjan.jarvis.model.run.AgentRunStatus;
import fr.karabodjan.jarvis.viewmodel.AgentListViewModel;
import fr.karabodjan.jarvis.viewmodel.AgentRunViewModel;
import fr.karabodjan.jarvis.viewmodel.RunHistoryViewModel;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainController {

    private static final DateTimeFormatter CREATED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
                    .withZone(ZoneId.systemDefault());

    private static final Map<AgentRunStatus, PseudoClass> PSEUDO_BY_STATUS = buildPseudoMap();

    private static Map<AgentRunStatus, PseudoClass> buildPseudoMap() {
        EnumMap<AgentRunStatus, PseudoClass> map = new EnumMap<>(AgentRunStatus.class);
        for (AgentRunStatus s : AgentRunStatus.values()) {
            map.put(s, PseudoClass.getPseudoClass(s.name().toLowerCase(Locale.ROOT)));
        }
        return map;
    }

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
    @FXML private Button cancelButton;
    @FXML private Button historyButton;
    @FXML private ListView<String> logListView;

    private AgentListViewModel listViewModel;
    private RunHistoryViewModel runHistoryViewModel;

    private AgentRunViewModel boundRun;

    private Stage historyStage;

    private final ChangeListener<AgentRunStatus> statusListener =
            (obs, oldStatus, newStatus) -> {
                applyStatusPseudoClass(detailStatusLabel, newStatus);
                refreshActionButtons(newStatus);
            };

    public void setViewModel(AgentListViewModel listViewModel) {
        this.listViewModel = Objects.requireNonNull(listViewModel, "listViewModel must not be null");
    }

    public void setRunHistoryViewModel(RunHistoryViewModel runHistoryViewModel) {
        this.runHistoryViewModel = Objects.requireNonNull(runHistoryViewModel, "runHistoryViewModel must not be null");
    }

    public void init() {
        Objects.requireNonNull(listViewModel, "setViewModel must be called before init");
        Objects.requireNonNull(runHistoryViewModel, "setRunHistoryViewModel must be called before init");

        agentListView.setItems(listViewModel.getAgents());
        agentListView.setCellFactory(listView -> new AgentRunCell());

        agentListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldRun, newRun) -> bindToRun(newRun)
        );

        // Bind the log console to the global log feed.
        logListView.setItems(listViewModel.getLogs());
        listViewModel.getLogs().addListener(
                (javafx.collections.ListChangeListener<String>) change ->
                        logListView.scrollTo(listViewModel.getLogs().size() - 1)
        );

        bindToRun(null);
    }

    // --- selection-driven binding --------------------------------------

    private void bindToRun(AgentRunViewModel runVm) {
        if (boundRun != null) {
            detailMessageLabel.textProperty().unbind();
            detailProgressBar.progressProperty().unbind();
            detailStatusLabel.textProperty().unbind();
            boundRun.statusProperty().removeListener(statusListener);
            boundRun = null;
        }

        if (runVm == null) {
            detailTitleLabel.setText("Select an agent");
            detailPlaceholderLabel.setVisible(true);
            detailPlaceholderLabel.setManaged(true);
            detailFieldsBox.setVisible(false);
            detailFieldsBox.setManaged(false);
            return;
        }

        var agent = runVm.getAgent();
        detailTitleLabel.setText(agent.getName());
        detailPlaceholderLabel.setVisible(false);
        detailPlaceholderLabel.setManaged(false);
        detailFieldsBox.setVisible(true);
        detailFieldsBox.setManaged(true);

        detailRepoLabel.setText(agent.getRepoUrl());
        detailTaskTypeLabel.setText(agent.getTaskType());
        detailCreatedAtLabel.setText(CREATED_AT_FORMAT.format(agent.getCreatedAt()));

        detailMessageLabel.textProperty().bind(runVm.messageProperty());
        detailProgressBar.progressProperty().bind(runVm.progressProperty());
        detailStatusLabel.textProperty().bind(runVm.statusProperty().asString());

        boundRun = runVm;
        runVm.statusProperty().addListener(statusListener);

        applyStatusPseudoClass(detailStatusLabel, runVm.getStatus());
        refreshActionButtons(runVm.getStatus());
    }

    private void refreshActionButtons(AgentRunStatus status) {
        boolean running = (status == AgentRunStatus.RUNNING);
        launchButton.setDisable(running);
        cancelButton.setVisible(running);
        cancelButton.setManaged(running);
    }

    private static void applyStatusPseudoClass(Node node, AgentRunStatus status) {
        for (Map.Entry<AgentRunStatus, PseudoClass> entry : PSEUDO_BY_STATUS.entrySet()) {
            node.pseudoClassStateChanged(entry.getValue(), entry.getKey() == status);
        }
    }

    // --- FXML handlers --------------------------------------------------

    @FXML
    private void onLaunchClicked() {
        if (boundRun == null) return;
        listViewModel.launch(boundRun);
    }

    @FXML
    private void onCancelClicked() {
        if (boundRun == null) return;
        listViewModel.cancel(boundRun);
    }

    @FXML
    private void onHistoryClicked() {
        try {
            if (historyStage == null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fr/karabodjan/jarvis/history-view.fxml")
                );
                Scene scene = new Scene(loader.load(), 900, 500);

                HistoryController historyController = loader.getController();
                historyController.setViewModel(runHistoryViewModel);
                historyController.init();

                historyStage = new Stage();
                historyStage.setTitle("J.A.R.V.I.S. — Run History");
                historyStage.setScene(scene);
            }
            runHistoryViewModel.refresh();
            historyStage.show();
            historyStage.toFront();
        } catch (IOException e) {
            System.err.println("[JARVIS] Failed to open history window: " + e.getMessage());
        }
    }

    // --- Sidebar cell with status dot ----------------------------------

    private static final class AgentRunCell extends ListCell<AgentRunViewModel> {

        private final Label dot = new Label("●");
        private final Label name = new Label();
        private final HBox layout = new HBox(dot, name);

        private AgentRunViewModel observed;

        private final ChangeListener<AgentRunStatus> dotListener =
                (obs, oldStatus, newStatus) -> applyStatusPseudoClass(dot, newStatus);

        AgentRunCell() {
            dot.getStyleClass().add("status-dot");
            layout.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(AgentRunViewModel runVm, boolean empty) {
            super.updateItem(runVm, empty);

            if (observed != null) {
                observed.statusProperty().removeListener(dotListener);
                observed = null;
            }

            if (empty || runVm == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            name.setText(runVm.getAgent().getName());
            applyStatusPseudoClass(dot, runVm.getStatus());
            runVm.statusProperty().addListener(dotListener);
            observed = runVm;

            setText(null);
            setGraphic(layout);
        }
    }
}