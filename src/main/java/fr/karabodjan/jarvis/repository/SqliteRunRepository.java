package fr.karabodjan.jarvis.repository;

import fr.karabodjan.jarvis.model.run.PersistedRun;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

    private static final String INSERT_RUN = """
            INSERT INTO agent_runs
                (run_id, agent_id, agent_name, status,
                 started_at, completed_at, pr_url, error_message, merged)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(INSERT_RUN)) {
            ps.setString(1, run.runId());
            ps.setString(2, run.agentId());
            ps.setString(3, run.agentName());
            ps.setString(4, run.status().name());         // enum por name(), nunca ordinal()
            ps.setString(5, run.startedAt().toString());  // Instant -> ISO-8601
            ps.setString(6, run.completedAt().toString());
            ps.setString(7, run.prUrl());                 // null -> SQL NULL
            ps.setString(8, run.errorMessage());          // null -> SQL NULL
            ps.setInt(9, run.merged() ? 1 : 0);           // boolean -> 0/1
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JarvisStorageException("Failed to save run " + run.runId(), e);
        }
    }

    @Override
    public List<PersistedRun> listRuns() {
        throw new UnsupportedOperationException("listRuns: implementado no próximo commit");
    }
}