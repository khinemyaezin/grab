package com.grab.store.identity.internal.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.ScopeDeclaration;
import com.grab.framework.security.SecurityManifest;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("migration")
@Testcontainers(disabledWithoutDocker = true)
class SecurityManifestMigrationIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void migrate_legacyIdentifiersGrantsAndLobPayload_arePreserved() throws Exception {
        String schema = "identity_upgrade";
        var source = database(schema, "8");
        var jdbc = new JdbcTemplate(source);
        jdbc.update("INSERT INTO authorities(uuid, code, category, name, active) VALUES ('existing-authority', 'MERCHANT_READ', 'merchant', 'Read', FALSE)");
        jdbc.update("INSERT INTO roles(uuid, code, name, role_kind, active, assignable) VALUES ('existing-role', 'OWNER', 'Owner', 'CUSTOM', TRUE, TRUE)");
        jdbc.update("INSERT INTO role_authorities(role_id, authority_id) SELECT r.id, a.id FROM roles r, authorities a WHERE r.uuid = 'existing-role' AND a.uuid = 'existing-authority'");
        var manifest = merchant(2, Lifecycle.RETIRED);
        String payload = new ObjectMapper().writeValueAsString(manifest);
        Long lob = jdbc.queryForObject("SELECT lo_from_bytea(0, convert_to(?, 'UTF8'))::bigint", Long.class, payload);
        jdbc.update("INSERT INTO security_manifest_module(module_key, applied_revision, applied_digest) VALUES ('merchant', 2, ?)", manifest.contentDigest());
        jdbc.update("INSERT INTO security_manifest_revision(module_key, revision, content_digest, status, event_id, payload, received_at, applied_at) "
                + "VALUES ('merchant', 2, ?, 'APPLIED', 'legacy', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", manifest.contentDigest(), lob.toString());

        Flyway.configure().dataSource(source).schemas(schema).locations("classpath:db/migration/identity").load().migrate();

        assertThat(jdbc.queryForObject("SELECT payload FROM security_manifest_revision", String.class)).isEqualTo(payload);
        assertThat(jdbc.queryForObject("SELECT uuid FROM authorities", String.class)).isEqualTo("existing-authority");
        assertThat(jdbc.queryForObject("SELECT owner_key FROM authorities", String.class)).isEqualTo("merchant");
        assertThat(jdbc.queryForObject("SELECT provider_lifecycle FROM authorities", String.class)).isEqualTo("RETIRED");
        assertThat(jdbc.queryForObject("SELECT active FROM authorities", Boolean.class)).isFalse();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM role_authorities", Long.class)).isEqualTo(1L);
    }

    @Test
    void migrate_existingScopeOwnershipCollision_requiresAuditCorrection() {
        String schema = "identity_collision";
        var source = database(schema, "8");
        var jdbc = new JdbcTemplate(source);
        jdbc.update("INSERT INTO security_scope_definitions(module_key, scope_key, manifest_version) VALUES ('merchant', 'merchant.account', 1), ('inventory', 'merchant.account', 1)");
        var flyway = Flyway.configure().dataSource(source).schemas(schema).locations("classpath:db/migration/identity").load();

        assertThatThrownBy(flyway::migrate).isInstanceOf(RuntimeException.class).hasStackTraceContaining("ownership collisions");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM security_scope_definitions", Long.class)).isEqualTo(2L);
    }

    private DataSource database(String schema, String target) {
        String jdbcUrl = DATABASE.getJdbcUrl();
        String separator = jdbcUrl.contains("?") ? "&" : "?";
        var source = new DriverManagerDataSource(jdbcUrl + separator + "currentSchema=" + schema,
                DATABASE.getUsername(), DATABASE.getPassword());
        Flyway.configure().dataSource(source).schemas(schema).locations("classpath:db/migration/identity")
                .target(target).load().migrate();
        return source;
    }

    private SecurityManifest merchant(int revision, Lifecycle lifecycle) {
        return new SecurityManifest("merchant", revision,
                List.of(new ScopeDeclaration("merchant.account", null, lifecycle)),
                List.of(new AuthorityDefinition("MERCHANT_READ", "Read", null, "merchant", lifecycle)));
    }
}
