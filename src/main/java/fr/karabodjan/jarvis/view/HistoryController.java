package fr.karabodjan.jarvis.view;

import fr.karabodjan.jarvis.model.run.PersistedRun;
import fr.karabodjan.jarvis.viewmodel.RunHistoryViewModel;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryController {

    private static final DateTimeFormatter DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    private static final String CSV_HEADER =
            "run_id,agent_id,agent_name,status,started_at,completed_at,pr_url,error_message,merged";

    @FXML private TableView<PersistedRun> historyTable;
    @FXML private TableColumn<PersistedRun, String> agentColumn;
    @FXML private TableColumn<PersistedRun, String> statusColumn;
    @FXML private TableColumn<PersistedRun, String> startedAtColumn;
    @FXML private TableColumn<PersistedRun, String> durationColumn;
    @FXML private TableColumn<PersistedRun, String> prUrlColumn;
    @FXML private ComboBox<String> agentFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;

    private RunHistoryViewModel viewModel;

    public void setViewModel(RunHistoryViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    }

    public void init() {
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

        viewModel.refresh();
    }

    private String formatDuration(Duration d) {
        long sec = d.getSeconds();
        if (sec < 60) return sec + "s";
        return (sec / 60) + "m " + (sec % 60) + "s";
    }

    // --- CSV export ----------------------------------------------------

    @FXML
    private void onExportClicked() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar histórico para CSV");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files", "*.csv")
        );
        chooser.setInitialFileName("jarvis-history-" + LocalDate.now() + ".csv");

        Window owner = exportButton.getScene().getWindow();
        File target = chooser.showSaveDialog(owner);
        if (target == null) {
            return; // utilizador cancelou
        }

        // Exporta APENAS o que está visível (respeita os filtros activos).
        List<PersistedRun> visible = new ArrayList<>(viewModel.getFilteredRuns());

        try {
            writeCsv(visible, target.toPath());
            showAlert(Alert.AlertType.INFORMATION, "Exportado",
                    visible.size() + " run(s) exportadas para:\n" + target.getAbsolutePath());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao exportar", e.getMessage());
        }
    }

    private void writeCsv(List<PersistedRun> runs, Path target) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            writer.write(CSV_HEADER);
            writer.newLine();
            for (PersistedRun r : runs) {
                writer.write(String.join(",",
                        csvEscape(r.runId()),
                        csvEscape(r.agentId()),
                        csvEscape(r.agentName()),
                        csvEscape(r.status().name()),
                        csvEscape(r.startedAt().toString()),
                        csvEscape(r.completedAt().toString()),
                        csvEscape(r.prUrl()),
                        csvEscape(r.errorMessage()),
                        r.merged() ? "1" : "0"
                ));
                writer.newLine();
            }
        }
    }


    private static String csvEscape(String value) {
        if (value == null) return "";
        if (value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}