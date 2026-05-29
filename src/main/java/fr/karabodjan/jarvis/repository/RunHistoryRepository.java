package fr.karabodjan.jarvis.repository;

import fr.karabodjan.jarvis.model.run.PersistedRun;

import java.util.List;

public interface RunHistoryRepository {


    void saveRun(PersistedRun run);

    List<PersistedRun> listRuns();

    void updateMerged(String runId);
}