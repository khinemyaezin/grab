package com.grab.app.workflow;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class WorkflowMigrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void migrationsCreateTheWorkflowContract() throws Exception {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration/workflows")
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            assertTrue(hasColumn(connection, "workflow_execution", "failure_reason"));
            assertTrue(hasColumn(connection, "workflow_node_execution", "attempt_id"));
            assertTrue(hasColumn(connection, "workflow_node_execution", "accepted_outcome_json"));
            assertTrue(hasColumn(connection, "workflow_signal_receipt", "payload_fingerprint"));
            assertTrue(hasColumn(connection, "workflow_signal_receipt", "outcome"));
            assertTrue(hasColumn(connection, "workflow_signal_receipt", "outcome_fingerprint"));
            assertTrue(hasColumn(connection, "workflow_execution", "transition_revision"));
            assertEquals(1, countIndexes(connection, "idx_workflow_signal_outcome_fingerprint"));
            assertTrue(hasColumn(connection, "workflow_signal_receipt", "fingerprint_version"));
            assertTrue(hasColumn(connection, "workflow_start_receipt", "fingerprint_version"));
            assertTrue(hasColumn(connection, "workflow_timer", "logical_operation_id"));
            assertTrue(hasColumn(connection, "workflow_timer", "claim_token"));
            assertTrue(hasColumn(connection, "workflow_timer", "lease_expires_at"));
            assertTrue(hasColumn(connection, "workflow_execution", "outputs_json"));
            assertTrue(hasColumn(connection, "workflow_execution", "choices_json"));
            assertTrue(hasColumn(connection, "workflow_execution", "accepted_signals_json"));
            assertEquals(0, countIndexes(connection, "uq_workflow_signal_operation_fingerprint"));
            assertEquals(1, countIndexes(connection, "idx_workflow_signal_operation_fingerprint"));
            assertEquals(1, countIndexes(connection, "uq_workflow_timer_generation"));
        }
    }

    @Test
    void migrate_oldFingerprint_preservesReceiptAndQuarantinesActiveExecution() throws Exception {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .defaultSchema("workflow_upgrade")
                .locations("classpath:db/migration/workflows")
                .target("2")
                .load()
                .migrate();
        try (Connection connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    insert into workflow_upgrade.workflow_execution
                        (id, definition_name, definition_version, payload_version, caller_scope,
                         status, input_json, cancellation_requested, version, created_at, updated_at)
                    values ('old', 'example', 1, 1, 'scope', 'WAITING', '"sku"', false, 0, now(), now())
                    """);
            statement.executeUpdate("""
                    insert into workflow_upgrade.workflow_start_receipt
                        (caller_scope, idempotency_key, workflow_name, definition_version,
                         input_fingerprint, execution_id, created_at)
                    values ('scope', 'key', 'example', 1, 'old-digest', 'old', now())
                    """);
        }

        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .defaultSchema("workflow_upgrade")
                .locations("classpath:db/migration/workflows")
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     select execution.status, execution.fingerprint_recovery_required,
                            receipt.fingerprint_version, receipt.input_fingerprint
                     from workflow_upgrade.workflow_execution execution
                     join workflow_upgrade.workflow_start_receipt receipt on receipt.execution_id = execution.id
                     where execution.id = 'old'
                     """)) {
            assertTrue(result.next());
            assertEquals("NEEDS_ATTENTION", result.getString(1));
            assertTrue(result.getBoolean(2));
            assertEquals(1, result.getInt(3));
            assertEquals("old-digest", result.getString(4));
        }
    }

    private static boolean hasColumn(Connection connection, String table, String column) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("select count(*) from information_schema.columns where table_schema = current_schema() and table_name = ? and column_name = ?")) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) == 1;
            }
        }
    }

    private static int countIndexes(Connection connection, String indexName) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("select count(*) from pg_indexes where schemaname = current_schema() and indexname = ?")) {
            statement.setString(1, indexName);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }
}
