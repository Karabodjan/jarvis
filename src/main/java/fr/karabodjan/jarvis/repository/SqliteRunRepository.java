package fr.karabodjan.jarvis.repository;

import fr.karabodjan.jarvis.model.run.PersistedRun;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SqliteRunRepository implements RunHistoryRepository {

    private final String jdbcUrl;

    private static final String CREATE_AGENT_RUNS = """
            CREATE TABLE IF NOT EXISTS agent_runs (
                run_id        TEXT    PRIMARY KEY,
                agent_id      TEXT    NOT NULL,
                agent_name    TEXT    NOT NULL,
                status        TEXT    NOT NULL,
                started_at    TEXT    NOT NULL,
                completed_at  TEXT    NOT NULL,
                pr_url        TEXT,
                error_message TEXT,
                merged        INTEGER NOT NULL DEFAULT 0
            )
            """;

    public SqliteRunRepository(String dbPath) {
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        initSchema();
    }

    private void initSchema() {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_AGENT_RUNS);
        } catch (SQLException e) {
            throw new JarvisStorageException("Failed to initialise database schema", e);
        }
    }

    @Override
    public void saveRun(PersistedRun run) {
        throw new UnsupportedOperationException("saveRun: implementado no próximo commit");
    }

    @Override
    public List<PersistedRun> listRuns() {
        throw new UnsupportedOperationException("listRuns: implementado no próximo commit");
    }
}