package fr.karabodjan.jarvis.viewmodel;

import fr.karabodjan.jarvis.model.run.PersistedRun;
import fr.karabodjan.jarvis.repository.JarvisStorageException;
import fr.karabodjan.jarvis.repository.RunHistoryRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class RunHistoryViewModel {

    /** Valor sentinel para "qualquer" nos filtros. */
    public static final String ALL = "Todos";

    private final RunHistoryRepository repository;

    // Lista raw das runs lidas da BD.
    private final ObservableList<PersistedRun> runs =
            FXCollections.observableArrayList();

    // Vista filtrada — é o que a TableView consome.
    private final FilteredList<PersistedRun> filteredRuns =
            new FilteredList<>(runs, r -> true);

    // Opções de filtro (observáveis p/ alimentar ComboBoxes).
    private final ObservableList<String> distinctAgents =
            FXCollections.observableArrayList(ALL);
    private final ObservableList<String> statusOptions =
            FXCollections.observableArrayList(ALL, "COMPLETED", "FAILED", "CANCELLED");

    // Filtros selecionados.
    private final StringProperty selectedAgent = new SimpleStringProperty(ALL);
    private final StringProperty selectedStatus = new SimpleStringProperty(ALL);

    public RunHistoryViewModel(RunHistoryRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
        // Reaplica o filtro sempre que um critério muda.
        selectedAgent.addListener((obs, o, n) -> updatePredicate());
        selectedStatus.addListener((obs, o, n) -> updatePredicate());
    }

    /** Recarrega o histórico da BD e refresca os agentes distintos. */
    public void refresh() {
        try {
            List<PersistedRun> fresh = repository.listRuns();
            runs.setAll(fresh);

            // Recalcula nomes únicos para o ComboBox.
            List<String> names = fresh.stream()
                    .map(PersistedRun::agentName)
                    .distinct()
                    .sorted()
                    .toList();
            distinctAgents.setAll(ALL);
            distinctAgents.addAll(names);

            updatePredicate();
        } catch (JarvisStorageException e) {
            System.err.println("[JARVIS] Failed to load run history: " + e.getMessage());
        }
    }

    private void updatePredicate() {
        String agent = selectedAgent.get();
        String status = selectedStatus.get();

        Predicate<PersistedRun> p = r -> true;
        if (agent != null && !ALL.equals(agent)) {
            p = p.and(r -> agent.equals(r.agentName()));
        }
        if (status != null && !ALL.equals(status)) {
            p = p.and(r -> status.equals(r.status().name()));
        }
        filteredRuns.setPredicate(p);
    }

    public ObservableList<PersistedRun> getFilteredRuns() { return filteredRuns; }
    public ObservableList<String> getDistinctAgents() { return distinctAgents; }
    public ObservableList<String> getStatusOptions() { return statusOptions; }
    public StringProperty selectedAgentProperty() { return selectedAgent; }
    public StringProperty selectedStatusProperty() { return selectedStatus; }
}