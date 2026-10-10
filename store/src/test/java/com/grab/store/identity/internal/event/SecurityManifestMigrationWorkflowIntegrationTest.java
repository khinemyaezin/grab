package com.grab.store.identity.internal.event;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.ScopeDeclaration;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.grab.framework.security.SecurityManifest;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import com.identity.application.model.write.RegisterSecurityManifestResult;
import com.grab.store.identity.internal.command.handler.RegisterSecurityManifestCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("migration")
@SpringJUnitConfig(SecurityManifestWorkflowIntegrationTest.Config.class)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SecurityManifestMigrationWorkflowIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> DATABASE = SecurityManifestWorkflowIntegrationTest.DATABASE;

    @Autowired private RegisterSecurityManifestCommandHandler registration;
    @Autowired @Qualifier("merchantPublication") private SecurityManifestPublicationPort publication;
    @Autowired @Qualifier("merchantTransactionManager") private PlatformTransactionManager merchantTransactions;
    @Autowired @Qualifier("identityDataSource") private DataSource identityDataSource;
    @Autowired @Qualifier("merchantDataSource") private DataSource merchantDataSource;
    private JdbcTemplate identity;
    private JdbcTemplate merchant;

    @BeforeEach
    void reset() {
        identity = new JdbcTemplate(identityDataSource);
        merchant = new JdbcTemplate(merchantDataSource);
        identity.execute("TRUNCATE security_manifest_conflict, security_manifest_inbox, security_manifest_revision, "
                + "security_manifest_module, security_catalog_state, security_scope_definitions, role_authorities, authorities, identity_outbox_event, roles, users, access_assignments CASCADE");
        merchant.execute("TRUNCATE merchant_outbox_events");
        merchant.execute("TRUNCATE security_manifest_publication");
    }

    @Test
    void publish_sameRevisionConflict_andLegacyRegression_areRejected() {
        var tx = new TransactionTemplate(merchantTransactions);
        tx.executeWithoutResult(status -> publication.enqueue(merchant(3, Lifecycle.ACTIVE)));
        var conflict = tx.execute(status -> publication.enqueue(merchant(3, Lifecycle.RETIRED)));
        assertThat(conflict).isEqualTo(SecurityManifestPublicationPort.PublicationResult.CONFLICT);
        assertThatThrownBy(() -> merchant.update("UPDATE security_manifest_publication SET security_revision = 2, content_digest = 'old'"))
                .isInstanceOf(RuntimeException.class);
        assertThat(merchant.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class)).isEqualTo(1);
    }

    @Test
    void register_replayAfterCommitAndInboxCleanup_doesNotRepeatActivation() {
        var manifest = merchant(2, Lifecycle.ACTIVE);
        register(manifest, "first");
        var activated = identity.queryForObject("SELECT applied_at FROM security_manifest_revision", java.sql.Timestamp.class).toInstant();
        register(manifest, "first");
        identity.update("DELETE FROM security_manifest_inbox");
        var repair = register(manifest, "repair");
        assertThat(repair.newlyActivated()).isFalse();
        assertThat(identity.queryForObject("SELECT catalog_revision FROM security_catalog_state", Long.class)).isEqualTo(1);
        assertThat(identity.queryForObject("SELECT count(*) FROM identity_outbox_event", Long.class)).isEqualTo(1);
        assertThat(identity.queryForObject("SELECT applied_at FROM security_manifest_revision", java.sql.Timestamp.class).toInstant()).isEqualTo(activated);
        var conflict = new SecurityManifest("merchant", 2, manifest.scopes(),
                List.of(new AuthorityDefinition("MERCHANT_READ", "Changed", null)));
        assertThat(register(conflict, "collision").outcome()).isEqualTo("QUARANTINED");
        assertThat(identity.queryForObject("SELECT content_digest FROM security_manifest_revision", String.class)).isEqualTo(manifest.contentDigest());
        assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_conflict", Long.class)).isEqualTo(1);
        assertThatThrownBy(() -> identity.update("UPDATE security_manifest_revision SET content_digest = 'tampered'"))
                .isInstanceOf(RuntimeException.class);
    }

    private RegisterSecurityManifestResult register(SecurityManifest manifest, String id) {
        var command = new RegisterSecurityManifestCommand(manifest, id);
        return registration.handle(command);
    }

    private SecurityManifest merchant(int revision, Lifecycle lifecycle) {
        return new SecurityManifest("merchant", revision,
                List.of(new ScopeDeclaration("merchant.account", null, lifecycle)),
                List.of(new AuthorityDefinition("MERCHANT_READ", "Read", null, "merchant", lifecycle)));
    }
}
