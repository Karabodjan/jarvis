package fr.karabodjan.jarvis.view;

import fr.karabodjan.jarvis.model.run.PersistedRun;
import fr.karabodjan.jarvis.viewmodel.RunHistoryViewModel;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class HistoryController {

    private static final DateTimeFormatter DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    @FXML private TableView<PersistedRun> historyTable;
    @FXML private TableColumn<PersistedRun, String> agentColumn;
    @FXML private TableColumn<PersistedRun, String> statusColumn;
    @FXML private TableColumn<PersistedRun, String> startedAtColumn;
    @FXML private TableColumn<PersistedRun, String> durationColumn;
    @FXML private TableColumn<PersistedRun, String> prUrlColumn;
    @FXML private ComboBox<String> agentFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button refreshButton;

    private RunHistoryViewModel viewModel;

    public void setViewModel(RunHistoryViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    }

    public void init() {
        // Cell value factories — lambdas porque PersistedRun é record (sem getters "get*").
        agentColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().agentName()));
        statusColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().status().name()));
        startedAtColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(DATETIME.format(cd.getValue().startedAt())));
        durationColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(formatDuration(cd.getValue().duration())));
        prUrlColumn.setCellValueFactory(cd -> {
            String url = cd.getValue().prUrl();
            return new ReadOnlyStringWrapper(url != null ? url : "");
        });

        historyTable.setItems(viewModel.getFilteredRuns());

        agentFilter.setItems(viewModel.getDistinctAgents());
        agentFilter.valueProperty().bindBidirectional(viewModel.selectedAgentProperty());

        statusFilter.setItems(viewModel.getStatusOptions());
        statusFilter.valueProperty().bindBidirectional(viewModel.selectedStatusProperty());

        refreshButton.setOnAction(e -> viewModel.refresh());

        // Carregamento inicial.
        viewModel.refresh();
    }

    private String formatDuration(Duration d) {
        long sec = d.getSeconds();
        if (sec < 60) return sec + "s";
        return (sec / 60) + "m " + (sec % 60) + "s";
    }
}