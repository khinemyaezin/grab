package com.grab.store.saleschannel.internal.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class SalesChannelV1MigrationTest {
    @Container
    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void freshV1CreatesPublishableKeyTableAndLocalForeignKey() throws SQLException {
        Flyway flyway = Flyway.configure()
                .dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
                .locations("classpath:db/migration/saleschannel")
                .baselineOnMigrate(false)
                .load();

        flyway.migrate();

        try (Connection connection = DATABASE.createConnection("")) {
            assertThat(tableExists(connection, "sales_channel_publishable_keys")).isTrue();
            assertThat(columnExists(connection, "sales_channel_publishable_keys", "token_digest")).isTrue();
            assertThat(foreignKeyExists(connection, "fk_publishable_key_sales_channel")).isTrue();
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (ResultSet result = connection.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"})) {
            return result.next();
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        try (ResultSet result = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
            return result.next();
        }
    }

    private boolean foreignKeyExists(Connection connection, String constraintName) throws SQLException {
        String sql = "select 1 from pg_constraint where conname = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, constraintName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
