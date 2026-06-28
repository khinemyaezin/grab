package com.grab.store.identity.internal.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class IdentityScopeMigrationTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("identity_migration")
            .withUsername("identity")
            .withPassword("identity");

    @Test
    void migrateV4_shouldRenameAndNamespaceScopesAndSeedDelegationRules() throws SQLException {
        String schema = "scope_migration";
        migrateToV3(schema);
        insertLegacyScopedData(schema, "MERCHANT_ACCOUNT");

        flyway(schema, null).migrate();

        try (Connection connection = connection(schema); Statement statement = connection.createStatement()) {
            assertThat(singleValue(statement,
                    "SELECT scope_key FROM access_assignments WHERE uuid = 'assignment-migration'"))
                    .isEqualTo("merchant.account");
            assertThat(singleValue(statement,
                    "SELECT scope_key FROM access_invitations WHERE uuid = 'invitation-migration'"))
                    .isEqualTo("merchant.storefront");
            assertThat(singleValue(statement,
                    "SELECT scope_key FROM refresh_sessions WHERE token_hash = '" + "c".repeat(64) + "'"))
                    .isEqualTo("inventory.fulfillment-location");
            assertThat(Integer.parseInt(singleValue(statement,
                    "SELECT count(*) FROM role_delegation_rules"))).isGreaterThan(0);
            assertThatThrownBy(() -> statement.executeUpdate("""
                    UPDATE access_assignments
                    SET scope_key = 'global', scope_id = 'merchant-1'
                    WHERE uuid = 'assignment-migration'
                    """))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void migrateV4_withUnknownLegacyScope_shouldStopMigration() throws SQLException {
        String schema = "unknown_scope_migration";
        migrateToV3(schema);
        insertLegacyScopedData(schema, "UNKNOWN_SCOPE");

        assertThatThrownBy(() -> flyway(schema, null).migrate())
                .isInstanceOf(FlywayException.class)
                .hasMessageContaining("Unknown legacy identity scope type");
    }

    private void migrateToV3(String schema) {
        flyway(schema, MigrationVersion.fromVersion("3")).migrate();
    }

    private Flyway flyway(String schema, MigrationVersion target) {
        var configuration = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration/identity")
                .schemas(schema)
                .createSchemas(true)
                .placeholders(Map.of(
                        "seedAdmin", "false",
                        "adminEmail", "unused@example.com",
                        "adminPasswordHash", "unused"
                ));
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    private Connection connection(String schema) throws SQLException {
        Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()
        );
        connection.setSchema(schema);
        return connection;
    }

    private void insertLegacyScopedData(String schema, String assignmentScope) throws SQLException {
        try (Connection connection = connection(schema); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO users (uuid, email, password_hash, status, created_at, updated_at)
                    VALUES ('migration-user', 'migration@example.com', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """);
            statement.executeUpdate("""
                    INSERT INTO access_assignments (
                        uuid, user_id, platform_role_id, scope_type, scope_id,
                        status, assigned_by, created_at, updated_at, expires_at
                    )
                    SELECT 'assignment-migration', users.id, platform_roles.id, '%s', 'merchant-1',
                           'ACTIVE', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL
                    FROM users
                    JOIN platforms ON platforms.code = 'SELLER_PORTAL'
                    JOIN roles ON roles.code = 'MERCHANT_OWNER'
                    JOIN platform_roles ON platform_roles.platform_id = platforms.id
                        AND platform_roles.role_id = roles.id
                    WHERE users.uuid = 'migration-user'
                    """.formatted(assignmentScope));
            statement.executeUpdate("""
                    INSERT INTO access_invitations (
                        uuid, invitee_email, platform_role_id, scope_type, scope_id, token_hash,
                        invited_by, status, created_at, updated_at, expires_at, accepted_by
                    )
                    SELECT 'invitation-migration', 'invitee@example.com', platform_roles.id,
                           'STOREFRONT', 'storefront-1', '%s', 'migration-user', 'PENDING',
                           CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 day', NULL
                    FROM platforms
                    JOIN roles ON roles.code = 'STOREFRONT_MANAGER'
                    JOIN platform_roles ON platform_roles.platform_id = platforms.id
                        AND platform_roles.role_id = roles.id
                    WHERE platforms.code = 'SELLER_PORTAL'
                    """.formatted("b".repeat(64)));
            statement.executeUpdate("""
                    INSERT INTO refresh_sessions (
                        user_id, token_hash, token_family_id, expires_at, created_at,
                        platform_code, assignment_uuid, scope_type, scope_id
                    )
                    SELECT id, '%s', 'migration-family', CURRENT_TIMESTAMP + INTERVAL '1 day',
                           CURRENT_TIMESTAMP, 'SELLER_PORTAL', 'assignment-migration',
                           'FULFILLMENT_LOCATION', 'location-1'
                    FROM users WHERE uuid = 'migration-user'
                    """.formatted("c".repeat(64)));
        }
    }

    private String singleValue(Statement statement, String sql) throws SQLException {
        try (ResultSet result = statement.executeQuery(sql)) {
            assertThat(result.next()).isTrue();
            return result.getString(1);
        }
    }
}
