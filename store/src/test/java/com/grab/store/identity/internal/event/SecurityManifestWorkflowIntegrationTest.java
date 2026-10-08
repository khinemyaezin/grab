package com.grab.store.identity.internal.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.impl.DefaultCommandBus;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.impl.DefaultQueryBus;
import com.grab.store.identity.port.IdentityLookupQuery;
import com.grab.store.identity.internal.query.handler.ListWaitingSecurityManifestCandidatesQueryHandler;
import com.identity.adapter.persistence.specification.jpa.SecurityManifestWaitingSpecification;
import com.identity.adapter.persistence.mapper.jpa.SecurityCatalogJpaAssembler;
import com.identity.application.port.outbound.SecurityManifestQueryPort;
import com.grab.outbox.infrastructure.security.SecurityManifestPublicationQueryAdapter;
import com.grab.framework.outbox.*;
import com.grab.framework.security.*;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.security.SecurityManifestPublicationAdapter;
import com.grab.store.identity.internal.command.handler.RegisterSecurityManifestCommandHandler;
import com.grab.store.identity.internal.command.handler.RevalidateSecurityManifestCommandHandler;
import com.identity.adapter.persistence.adapter.*;
import com.identity.adapter.persistence.entity.*;
import com.identity.adapter.persistence.outbox.IdentityOutboxEvent;
import com.identity.adapter.persistence.outbox.IdentityOutboxEventProducer;
import com.identity.adapter.persistence.repository.jpa.*;
import com.identity.application.model.write.*;
import com.identity.application.port.inbound.*;
import com.identity.application.service.*;
import com.identity.domain.port.outbound.*;
import com.grab.store.shared.events.merchant.MerchantSecurityManifestDeclaredIntegrationEvent;
import com.grab.store.shared.events.identity.IdentitySecurityManifestDeclaredIntegrationEvent;
import com.merchant.adapter.persistence.entity.MerchantSecurityManifestPublicationEntity;
import com.merchant.adapter.persistence.outbox.MerchantOutboxEvent;
import com.merchant.adapter.persistence.outbox.MerchantOutboxEventProducer;
import com.merchant.adapter.persistence.repository.jpa.MerchantSecurityManifestPublicationJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

@SpringJUnitConfig(SecurityManifestWorkflowIntegrationTest.Config.class)
class SecurityManifestWorkflowIntegrationTest {
    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:16-alpine");
    static { DATABASE.start(); }

    @Autowired private RegisterSecurityManifestCommandHandler registration;
    @Autowired private RevalidateSecurityManifestCommandHandler revalidation;
    @Autowired private SecurityManifestRevisionRepository revisions;
    @Autowired private SecurityCatalogRepository catalogs;
    @Autowired private CommandBus commands;
    @Autowired @Qualifier("merchantPublication") private SecurityManifestPublicationPort publication;
    @Autowired @Qualifier("identityTransactionManager") private PlatformTransactionManager identityTransactions;
    @Autowired @Qualifier("merchantTransactionManager") private PlatformTransactionManager merchantTransactions;
    @Autowired @Qualifier("identityDataSource") private DataSource identityDataSource;
    @Autowired @Qualifier("merchantDataSource") private DataSource merchantDataSource;
    @Autowired @Qualifier("identityOutboxStore") private OutboxStore<IdentityOutboxEvent, Long> identityOutbox;
    @Autowired @Qualifier("merchantOutboxStore") private OutboxStore<MerchantOutboxEvent, Long> merchantOutbox;
    @Autowired @Qualifier("replicaOne") private IdentityLookupQuery replicaOne;
    @Autowired @Qualifier("replicaTwo") private IdentityLookupQuery replicaTwo;
    @Autowired private SecurityManifestQueryPort manifestQueries;
    @Autowired private IdentitySecurityManifestRevalidationScheduler scheduler;
    private JdbcTemplate identity;
    private JdbcTemplate merchant;

    @BeforeEach
    void reset() {
        identity = new JdbcTemplate(identityDataSource);
        merchant = new JdbcTemplate(merchantDataSource);
        identity.execute("TRUNCATE security_manifest_conflict, security_manifest_inbox, security_manifest_revision, "
                + "security_manifest_module, security_scope_definitions, role_authorities, authorities, identity_outbox_event, roles, users, access_assignments CASCADE");
        identity.update("UPDATE security_catalog_state SET catalog_revision = 0 WHERE id = 1");
        merchant.execute("TRUNCATE merchant_outbox_events");
        merchant.execute("TRUNCATE security_manifest_publication");
        merchant.update("INSERT INTO security_manifest_publication(module_key, security_revision, content_digest) VALUES ('merchant', 0, '')");
    }

    @Test
    void publish_rollbackAndCrashGap_preservesOnlyCommittedEnqueue() {
        var manifest = merchant(2, Lifecycle.ACTIVE);
        var tx = new TransactionTemplate(merchantTransactions);
        assertThatThrownBy(() -> tx.execute(status -> {
            publication.enqueue(manifest);
            throw new IllegalStateException("injected owner crash");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(merchant.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class)).isZero();
        assertThat(merchant.queryForObject("SELECT security_revision FROM security_manifest_publication", Integer.class)).isZero();
        tx.executeWithoutResult(status -> publication.enqueue(manifest));
        assertThat(merchant.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class)).isEqualTo(1);
        var listener = new IdentitySecurityManifestRegistrationListener(commands);
        var relay = new Relay<>(merchantOutbox, new JsonOutboxEventSerializer(),
                event -> listener.onSecurityManifestDeclared((SecurityManifestDeclaredIntegrationEvent) event), merchantTransactions);
        relay.process();
        assertThat(applied("merchant")).isEqualTo(2);
        assertThat(merchant.queryForObject("SELECT status FROM merchant_outbox_events", String.class)).isEqualTo("PUBLISHED");
    }

    @Test
    void publish_concurrentReplicasAndOldBinary_enqueueOnceWithoutDowngrade() throws Exception {
        var manifest = merchant(3, Lifecycle.ACTIVE);
        var ready = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<SecurityManifestPublicationPort.PublicationResult> worker = () -> {
                ready.await();
                return new TransactionTemplate(merchantTransactions).execute(status -> publication.enqueue(manifest));
            };
            var first = pool.submit(worker);
            var second = pool.submit(worker);
            ready.countDown();
            assertThat(List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(SecurityManifestPublicationPort.PublicationResult.ENQUEUED,
                            SecurityManifestPublicationPort.PublicationResult.NOT_DUE);
        }
        merchant.update("UPDATE security_manifest_publication SET lease_until = CURRENT_TIMESTAMP - INTERVAL '1 minute'");
        var result = new TransactionTemplate(merchantTransactions).execute(status -> publication.enqueue(merchant(2, Lifecycle.ACTIVE)));
        assertThat(result).isEqualTo(SecurityManifestPublicationPort.PublicationResult.SUPERSEDED);
        assertThat(merchant.queryForObject("SELECT security_revision FROM security_manifest_publication", Integer.class)).isEqualTo(3);
        assertThat(merchant.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class)).isEqualTo(1);
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
        Instant activated = identity.queryForObject("SELECT applied_at FROM security_manifest_revision", java.sql.Timestamp.class).toInstant();
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

    @Test
    void register_digestMismatch_doesNotPinRevisionOrOverwriteOriginalReceipt() {
        var manifest = merchant(2, Lifecycle.ACTIVE);
        register(manifest, "original");
        var bad = new RegisterSecurityManifestCommand(manifest, "original", "wrong-digest", Instant.now());
        assertThat(registration.handle(bad).outcome()).isEqualTo("QUARANTINED");
        assertThat(identity.queryForObject("SELECT status FROM security_manifest_inbox", String.class)).isEqualTo("APPLIED");
        assertThat(identity.queryForObject("SELECT status FROM security_manifest_revision", String.class)).isEqualTo("APPLIED");
    }

    @Test
    void register_failureDuringBundle_rollsBackEveryIdentityWrite() {
        var manifest = merchant(2, Lifecycle.ACTIVE);
        identity.execute("CREATE FUNCTION fail_scope_write() RETURNS trigger LANGUAGE plpgsql AS $$ "
                + "BEGIN RAISE EXCEPTION 'injected scope write failure'; END $$");
        identity.execute("CREATE TRIGGER fail_scope BEFORE INSERT ON security_scope_definitions "
                + "FOR EACH ROW EXECUTE FUNCTION fail_scope_write()");
        try {
            assertThatThrownBy(() -> register(manifest, "failed")).isInstanceOf(RuntimeException.class);
            for (String table : List.of("authorities", "security_manifest_module", "security_manifest_inbox",
                    "security_manifest_revision", "identity_outbox_event")) {
                assertThat(identity.queryForObject("SELECT count(*) FROM " + table, Long.class)).isZero();
            }
            assertThat(identity.queryForObject("SELECT catalog_revision FROM security_catalog_state", Long.class)).isZero();
        } finally {
            identity.execute("DROP TRIGGER fail_scope ON security_scope_definitions");
            identity.execute("DROP FUNCTION fail_scope_write()");
        }
        assertThat(register(manifest, "failed").newlyActivated()).isTrue();
    }

    @Test
    void register_nullRootParentAndRetirement_areVisibleAcrossFreshSnapshots() {
        register(merchant(2, Lifecycle.ACTIVE), "merchant");
        var inventory = new SecurityManifest("inventory", 2,
                List.of(new ScopeDeclaration("inventory.location", "merchant.account")), List.of());
        assertThat(register(inventory, "inventory").newlyActivated()).isTrue();
        var scopes = new ScopeCatalogQueryAdapter(scopeRepository);
        var tx = new TransactionTemplate(identityTransactions);
        var firstReplica = tx.execute(status -> scopes.load().hierarchy());
        assertThat(firstReplica.isEffective("inventory.location")).isTrue();
        register(merchant(3, Lifecycle.RETIRED), "retirement");
        var secondReplica = tx.execute(status -> scopes.load().hierarchy());
        assertThat(secondReplica.isEffective("inventory.location")).isFalse();
        assertThat(secondReplica.isEffective("unknown.scope")).isFalse();
    }

    @Autowired private ScopeManifestJpaRepository scopeRepository;

    @Test
    void register_quarantinedHigherRevision_doesNotFenceValidLowerRevision() {
        var invalid = new SecurityManifest("merchant", 9,
                List.of(new ScopeDeclaration("merchant.account", "merchant.account")), List.of());
        assertThat(register(invalid, "invalid").outcome()).isEqualTo("QUARANTINED");
        assertThat(register(merchant(2, Lifecycle.ACTIVE), "valid").newlyActivated()).isTrue();
        assertThat(applied("merchant")).isEqualTo(2);
    }

    @Test
    void revalidate_oneFailure_doesNotRollBackAnotherCandidate() {
        var first = new SecurityManifest("inventory", 2,
                List.of(new ScopeDeclaration("inventory.location", "merchant.account")), List.of(),
                List.of(new SecurityDependency("merchant.account", 2)));
        var second = new SecurityManifest("catalog", 2,
                List.of(new ScopeDeclaration("catalog.store", "merchant.account")), List.of(),
                List.of(new SecurityDependency("merchant.account", 2)));
        assertThat(register(first, "waiting-1").outcome()).isEqualTo("WAITING_DEPENDENCY");
        assertThat(register(second, "waiting-2").outcome()).isEqualTo("WAITING_DEPENDENCY");
        register(merchant(2, Lifecycle.ACTIVE), "parent");
        identity.execute("CREATE FUNCTION fail_inventory_scope() RETURNS trigger LANGUAGE plpgsql AS $$ "
                + "BEGIN IF NEW.module_key = 'inventory' THEN RAISE EXCEPTION 'injected failure'; END IF; RETURN NEW; END $$");
        identity.execute("CREATE TRIGGER fail_inventory BEFORE INSERT ON security_scope_definitions "
                + "FOR EACH ROW EXECUTE FUNCTION fail_inventory_scope()");
        try {
            assertThatThrownBy(() -> revalidation.handle(new RevalidateSecurityManifestCommand("inventory", 2)))
                    .isInstanceOf(RuntimeException.class);
            scheduler.revalidate();
            assertThat(applied("catalog")).isEqualTo(2);
            assertThat(identity.queryForObject("SELECT status FROM security_manifest_revision WHERE module_key = 'inventory'", String.class))
                    .isEqualTo("WAITING_DEPENDENCY");
        } finally {
            identity.execute("DROP TRIGGER fail_inventory ON security_scope_definitions");
            identity.execute("DROP FUNCTION fail_inventory_scope()");
        }
        assertThat(revalidation.handle(new RevalidateSecurityManifestCommand("inventory", 2)).newlyActivated()).isTrue();
    }

    @Test
    void relay_identityOwnManifest_failureCanCommitRetryStateAndRecover() {
        var manifest = new SecurityManifest("identity", 2, List.of(),
                List.of(new AuthorityDefinition("IDENTITY_READ", "Read", null)));
        var serializer = new JsonOutboxEventSerializer();
        var event = new IdentitySecurityManifestDeclaredIntegrationEvent(manifest, "self");
        var producer = new IdentityOutboxEventProducer(identityOutbox, serializer);
        new TransactionTemplate(identityTransactions).executeWithoutResult(status -> producer.produce("SecurityManifest", "identity", List.of(event)));
        var fail = new AtomicBoolean(true);
        var relay = new Relay<>(identityOutbox, serializer, payload -> {
            if (fail.get()) {
                registration.handle(new RegisterSecurityManifestCommand(manifest, "self"));
                throw new IllegalStateException("failure after independent identity commit");
            } else if (payload instanceof SecurityManifestDeclaredIntegrationEvent declaration) {
                registration.handle(new RegisterSecurityManifestCommand(declaration.manifest(), declaration.eventId()));
            }
        }, identityTransactions);
        relay.process();
        assertThat(applied("identity")).isEqualTo(2);
        assertThat(identity.queryForObject("SELECT status FROM identity_outbox_event WHERE aggregate_type = 'SecurityManifest'", String.class)).isEqualTo("FAILED");
        fail.set(false);
        relay.process();
        assertThat(identity.queryForObject("SELECT status FROM identity_outbox_event WHERE aggregate_type = 'SecurityManifest'", String.class)).isEqualTo("PUBLISHED");
        assertThat(identity.queryForObject("SELECT catalog_revision FROM security_catalog_state", Long.class)).isEqualTo(1);
    }

    @Test
    void relay_identityOwnManifest_databaseFailure_doesNotPoisonSourceTransaction() {
        var manifest = new SecurityManifest("identity", 2, List.of(),
                List.of(new AuthorityDefinition("IDENTITY_READ", "Read", null)));
        var serializer = new JsonOutboxEventSerializer();
        var producer = new IdentityOutboxEventProducer(identityOutbox, serializer);
        var event = new IdentitySecurityManifestDeclaredIntegrationEvent(manifest, "self-failure");
        new TransactionTemplate(identityTransactions).executeWithoutResult(status -> producer.produce("SecurityManifest", "identity", List.of(event)));
        identity.execute("CREATE FUNCTION fail_authority_write() RETURNS trigger LANGUAGE plpgsql AS $$ "
                + "BEGIN RAISE EXCEPTION 'injected authority write failure'; END $$");
        identity.execute("CREATE TRIGGER fail_authority BEFORE INSERT ON authorities FOR EACH ROW EXECUTE FUNCTION fail_authority_write()");
        var listener = new IdentitySecurityManifestRegistrationListener(commands);
        var relay = new Relay<>(identityOutbox, serializer, payload -> {
            if (payload instanceof SecurityManifestDeclaredIntegrationEvent declaration) listener.onSecurityManifestDeclared(declaration);
        }, identityTransactions);
        try {
            relay.process();
            assertThat(identity.queryForObject("SELECT status FROM identity_outbox_event", String.class)).isEqualTo("FAILED");
            assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_inbox", Long.class)).isZero();
        } finally {
            identity.execute("DROP TRIGGER fail_authority ON authorities");
            identity.execute("DROP FUNCTION fail_authority_write()");
        }
        relay.process();
        assertThat(applied("identity")).isEqualTo(2);
        assertThat(identity.queryForObject("SELECT status FROM identity_outbox_event WHERE aggregate_type = 'SecurityManifest'", String.class)).isEqualTo("PUBLISHED");
    }

    @Test
    void authorize_twoReplicas_observeRetirementDespiteSuspendedOldSnapshot() {
        var active = merchant(2, Lifecycle.ACTIVE);
        register(active, "authorization-baseline");
        identity.update("INSERT INTO users(uuid, email, status, created_at, updated_at) VALUES ('user-1', 'owner@example.com', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        identity.update("INSERT INTO roles(uuid, code, name, role_kind, active, assignable) VALUES ('role-1', 'MERCHANT_OWNER', 'Owner', 'CUSTOM', TRUE, TRUE)");
        identity.update("INSERT INTO role_authorities(role_id, authority_id) SELECT r.id, a.id FROM roles r, authorities a WHERE r.uuid = 'role-1' AND a.code = 'MERCHANT_READ'");
        identity.update("INSERT INTO access_assignments(uuid, user_id, role_id, scope_key, scope_id, status, created_at, updated_at) "
                + "SELECT 'assignment-1', u.id, r.id, 'merchant.account', 'merchant-1', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM users u, roles r WHERE u.uuid = 'user-1' AND r.uuid = 'role-1'");
        var context = new AccessContext("assignment-1", "merchant.account", "merchant-1");
        assertThat(replicaOne.resolveByPlatformUserId("local", "user-1", context).orElseThrow().authorities()).containsExactly("MERCHANT_READ");
        String authorityId = identity.queryForObject("SELECT uuid FROM authorities WHERE code = 'MERCHANT_READ'", String.class);
        identity.update("UPDATE authorities SET active = FALSE WHERE code = 'MERCHANT_READ'");
        assertThat(replicaTwo.resolveByPlatformUserId("local", "user-1", context).orElseThrow().authorities()).isEmpty();
        identity.update("UPDATE authorities SET active = TRUE WHERE code = 'MERCHANT_READ'");
        var authorityRetired = new SecurityManifest("merchant", 3, active.scopes(), merchant(3, Lifecycle.RETIRED).authorities());
        register(authorityRetired, "authority-retired");
        assertThat(replicaOne.resolveByPlatformUserId("local", "user-1", context).orElseThrow().authorities()).isEmpty();
        assertThat(replicaTwo.resolveByPlatformUserId("local", "user-1", context).orElseThrow().authorities()).isEmpty();
        assertThat(identity.queryForObject("SELECT uuid FROM authorities WHERE code = 'MERCHANT_READ'", String.class)).isEqualTo(authorityId);
        assertThat(identity.queryForObject("SELECT count(*) FROM role_authorities", Long.class)).isEqualTo(1);
        var outer = new TransactionTemplate(identityTransactions);
        outer.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
        outer.executeWithoutResult(status -> {
            assertThat(identity.queryForObject("SELECT provider_lifecycle FROM security_scope_definitions", String.class)).isEqualTo("ACTIVE");
            register(merchant(4, Lifecycle.RETIRED), "scope-retired");
            assertThatThrownBy(() -> replicaOne.resolveByPlatformUserId("local", "user-1", context)).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(() -> replicaTwo.resolveByPlatformUserId("local", "user-1", context)).isInstanceOf(RuntimeException.class);
            assertThat(identity.queryForObject("SELECT provider_lifecycle FROM security_scope_definitions", String.class)).isEqualTo("ACTIVE");
        });
    }

    @Test
    void recover_activationOutbox_revalidatesBoundedWaitingCandidatesAndExposesStatus() {
        var candidate = new SecurityManifest("inventory", 2,
                List.of(new ScopeDeclaration("inventory.location", "merchant.account")), List.of(),
                List.of(new SecurityDependency("merchant.account", 2)));
        register(candidate, "activation-wait");
        var tx = new TransactionTemplate(identityTransactions);
        var waiting = tx.execute(status -> manifestQueries.status("inventory"));
        assertThat(waiting.waitingSince()).isNotNull();
        register(merchant(2, Lifecycle.ACTIVE), "activation-parent");
        var activation = new IdentitySecurityCatalogActivationListener(event -> { }, scheduler);
        var relay = new Relay<>(identityOutbox, new JsonOutboxEventSerializer(), payload -> {
            if (payload instanceof com.identity.domain.event.SecurityCatalogActivatedEvent changed) {
                activation.onCatalogChanged(new com.grab.store.shared.events.identity.IdentitySecurityCatalogActivatedIntegrationEvent(
                        changed.catalogRevision(), changed.moduleKey(), changed.securityRevision(), changed.contentDigest()));
            }
        }, identityTransactions);
        relay.process();
        assertThat(applied("inventory")).isEqualTo(2);
        var status = tx.execute(transaction -> manifestQueries.status("inventory"));
        assertThat(status.waitingSince()).isNull();
        assertThat(status.appliedDigest()).isEqualTo(candidate.contentDigest());
        assertThat(status.catalogRevision()).isEqualTo(2);
        var merchantTx = new TransactionTemplate(merchantTransactions);
        merchantTx.executeWithoutResult(transaction -> publication.enqueue(merchant(2, Lifecycle.ACTIVE)));
        var backlog = merchantTx.execute(transaction -> {
            EntityManager manager = SharedEntityManagerCreator.createSharedEntityManager(merchantFactory);
            var query = new SecurityManifestPublicationQueryAdapter(manager, MerchantSecurityManifestPublicationEntity.class, "MerchantOutboxEvent");
            return query.status("merchant");
        });
        assertThat(backlog.pendingCount()).isEqualTo(1);
        assertThat(backlog.oldestPendingAt()).isNotNull();
        assertThat(backlog.enqueuedRevision()).isEqualTo(2);
    }

    @Autowired @Qualifier("merchantEntityManagerFactory") private EntityManagerFactory merchantFactory;

    @Test
    void register_catalogLock_contentionLeavesNoReceipt() throws Exception {
        try (var pool = Executors.newSingleThreadExecutor()) {
            var locked = new CountDownLatch(1);
            var release = new CountDownLatch(1);
            var holder = pool.submit(() -> new TransactionTemplate(identityTransactions).executeWithoutResult(status -> {
                catalogs.loadForUpdate();
                locked.countDown();
                try { release.await(10, TimeUnit.SECONDS); } catch (InterruptedException ex) { throw new IllegalStateException(ex); }
            }));
            assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();
            try {
                assertThatThrownBy(() -> register(merchant(2, Lifecycle.ACTIVE), "contended")).isInstanceOf(RuntimeException.class);
                assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_inbox", Long.class)).isZero();
            } finally { release.countDown(); }
            holder.get(10, TimeUnit.SECONDS);
        }
        assertThat(register(merchant(2, Lifecycle.ACTIVE), "contended").newlyActivated()).isTrue();
    }

    @Test
    void migrate_legacyIdentifiersGrantsAndLobPayload_arePreserved() throws Exception {
        String schema = "identity_upgrade";
        var source = new Config().database(schema, "identity", "8");
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
        assertThat(jdbc.queryForObject("SELECT count(*) FROM role_authorities", Long.class)).isEqualTo(1);
    }

    @Test
    void migrate_existingScopeOwnershipCollision_requiresAuditCorrection() {
        String schema = "identity_collision";
        var source = new Config().database(schema, "identity", "8");
        var jdbc = new JdbcTemplate(source);
        jdbc.update("INSERT INTO security_scope_definitions(module_key, scope_key, manifest_version) VALUES ('merchant', 'merchant.account', 1), ('inventory', 'merchant.account', 1)");
        var flyway = Flyway.configure().dataSource(source).schemas(schema).locations("classpath:db/migration/identity").load();
        assertThatThrownBy(flyway::migrate).isInstanceOf(RuntimeException.class).hasStackTraceContaining("ownership collisions");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM security_scope_definitions", Long.class)).isEqualTo(2);
    }

    private int applied(String module) {
        return identity.queryForObject("SELECT applied_revision FROM security_manifest_module WHERE module_key = ?", Integer.class, module);
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

    private static final class Relay<T extends OutboxEntry<Long>> extends AbstractOutboxProcessor<T, Long> {
        Relay(OutboxStore<T, Long> store, OutboxEventSerializer serializer, OutboxEventDispatcher dispatcher,
              PlatformTransactionManager transactions) {
            super(store, serializer, dispatcher, transactions, 20, Duration.ZERO, Duration.ofMinutes(1), Duration.ofDays(7));
        }
        void process() { processAvailableEvents(); }
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @Import({IdentityRepositories.class, MerchantRepositories.class})
    static class Config {
        @Bean("identityDataSource") DataSource identityDataSource() { return database("identity", "identity"); }
        @Bean("merchantDataSource") DataSource merchantDataSource() { return database("merchant", "merchant"); }
        private DataSource database(String schema, String migration) { return database(schema, migration, null); }
        private DataSource database(String schema, String migration, String target) {
            String jdbcUrl = DATABASE.getJdbcUrl();
            String separator = jdbcUrl.contains("?") ? "&" : "?";
            var source = new DriverManagerDataSource(jdbcUrl + separator + "currentSchema=" + schema, DATABASE.getUsername(), DATABASE.getPassword());
            var flyway = Flyway.configure().dataSource(source).schemas(schema).locations("classpath:db/migration/" + migration);
            if (target != null) flyway.target(target);
            flyway.load().migrate();
            return source;
        }
        @Bean("identityEntityManagerFactory") LocalContainerEntityManagerFactoryBean identityFactory(@Qualifier("identityDataSource") DataSource source) {
            return factory(source, "identity", "com.identity.adapter.persistence.entity", "com.identity.adapter.persistence.outbox");
        }
        @Bean("merchantEntityManagerFactory") LocalContainerEntityManagerFactoryBean merchantFactory(@Qualifier("merchantDataSource") DataSource source) {
            return factory(source, "merchant", "com.merchant.adapter.persistence.entity", "com.merchant.adapter.persistence.outbox");
        }
        private LocalContainerEntityManagerFactoryBean factory(DataSource source, String unit, String... packages) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(source);
            factory.setPersistenceUnitName(unit);
            factory.setPackagesToScan(packages);
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "none"));
            return factory;
        }
        @Bean("identityTransactionManager") PlatformTransactionManager identityTransactions(@Qualifier("identityEntityManagerFactory") EntityManagerFactory factory) { return new JpaTransactionManager(factory); }
        @Bean("merchantTransactionManager") PlatformTransactionManager merchantTransactions(@Qualifier("merchantEntityManagerFactory") EntityManagerFactory factory) { return new JpaTransactionManager(factory); }
        @Bean("identityOutboxStore") OutboxStore<IdentityOutboxEvent, Long> identityOutbox(@Qualifier("identityEntityManagerFactory") EntityManagerFactory factory) {
            EntityManager entityManager = SharedEntityManagerCreator.createSharedEntityManager(factory);
            return new JpaOutboxStore<>(entityManager, IdentityOutboxEvent.class);
        }
        @Bean("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> merchantOutbox(@Qualifier("merchantEntityManagerFactory") EntityManagerFactory factory) {
            EntityManager entityManager = SharedEntityManagerCreator.createSharedEntityManager(factory);
            return new JpaOutboxStore<>(entityManager, MerchantOutboxEvent.class);
        }
        @Bean ObjectMapper objectMapper() { return JsonMapper.builder().addModule(new JavaTimeModule()).build(); }
        @Bean IdGenerator ids() { return new UuidGenerator(); }
        @Bean("identityEvents") DomainEventProducer events(@Qualifier("identityOutboxStore") OutboxStore<IdentityOutboxEvent, Long> store) { return new IdentityOutboxEventProducer(store, new JsonOutboxEventSerializer()); }
        @Bean SecurityCatalogRepository catalogs(SecurityCatalogStateJpaRepository states, SecurityManifestModuleJpaRepository modules,
                ScopeManifestJpaRepository scopes, AuthorityJpaRepository authorities, IdGenerator ids, @Qualifier("identityEvents") DomainEventProducer outbox) {
            return new SecurityCatalogRepositoryAdapter(states, modules, scopes, authorities, new SecurityCatalogJpaAssembler(ids), outbox, new IdentityPersistenceExecutor());
        }
        @Bean SecurityManifestRevisionRepository revisions(SecurityManifestRevisionJpaRepository repository, ObjectMapper mapper) { return new SecurityManifestRevisionRepositoryAdapter(repository, mapper); }
        @Bean SecurityManifestInboxRepository inbox(SecurityManifestInboxJpaRepository repository, SecurityManifestConflictJpaRepository conflicts, ObjectMapper mapper) { return new SecurityManifestInboxRepositoryAdapter(repository, conflicts, mapper); }
        @Bean RegisterSecurityManifestUseCase registration(SecurityCatalogRepository catalogs, SecurityManifestRevisionRepository revisions, SecurityManifestInboxRepository inbox) { return new RegisterSecurityManifestService(catalogs, revisions, inbox); }
        @Bean RevalidateSecurityManifestUseCase revalidation(SecurityCatalogRepository catalogs, SecurityManifestRevisionRepository revisions, RegisterSecurityManifestUseCase registration) { return new RevalidateSecurityManifestService(catalogs, revisions, registration); }
        @Bean RegisterSecurityManifestCommandHandler registrationHandler(RegisterSecurityManifestUseCase registration) { return new RegisterSecurityManifestCommandHandler(registration); }
        @Bean RevalidateSecurityManifestCommandHandler revalidationHandler(RevalidateSecurityManifestUseCase revalidation) { return new RevalidateSecurityManifestCommandHandler(revalidation); }
        @Bean ScopeCatalogQueryAdapter scopeCatalog(ScopeManifestJpaRepository repository) { return new ScopeCatalogQueryAdapter(repository); }
        @Bean IdentityLookupUseCase lookup(UserJpaRepository users, ExternalIdentityJpaRepository external,
                ExternalEntitlementMappingJpaRepository mappings, AccessAssignmentJpaRepository assignments, ScopeCatalogQueryAdapter scopes) {
            var adapter = new com.identity.adapter.persistence.adapter.IdentityLookupQueryAdapter(users, external, mappings, assignments, scopes);
            return new IdentityLookupService(adapter);
        }
        @Bean("replicaOne") IdentityLookupQuery replicaOne(IdentityLookupUseCase lookup) { return new com.grab.store.identity.internal.api.adapter.IdentityLookupQueryAdapter(lookup); }
        @Bean("replicaTwo") IdentityLookupQuery replicaTwo(IdentityLookupUseCase lookup) { return new com.grab.store.identity.internal.api.adapter.IdentityLookupQueryAdapter(lookup); }
        @Bean SecurityManifestQueryPort queries(@Qualifier("identityEntityManagerFactory") EntityManagerFactory factory,
                SecurityManifestRevisionJpaRepository revisions, SecurityManifestModuleJpaRepository modules,
                SecurityCatalogStateJpaRepository states, SecurityManifestConflictJpaRepository conflicts) {
            var manager = SharedEntityManagerCreator.createSharedEntityManager(factory);
            var specification = new SecurityManifestWaitingSpecification(manager);
            return new SecurityManifestQueryAdapter(specification, revisions, modules, states, conflicts);
        }
        @Bean ListWaitingSecurityManifestCandidatesUseCase waiting(SecurityManifestQueryPort queries) { return new ListWaitingSecurityManifestCandidatesService(queries); }
        @Bean ListWaitingSecurityManifestCandidatesQueryHandler waitingHandler(ListWaitingSecurityManifestCandidatesUseCase waiting) { return new ListWaitingSecurityManifestCandidatesQueryHandler(waiting); }
        @Bean QueryBus queryBus(ListWaitingSecurityManifestCandidatesQueryHandler handler) { return new DefaultQueryBus(List.of(handler)); }
        @Bean IdentitySecurityManifestRevalidationScheduler scheduler(QueryBus queries, CommandBus commands) { return new IdentitySecurityManifestRevalidationScheduler(queries, commands, 100); }
        @Bean CommandBus commands(RegisterSecurityManifestCommandHandler registration, RevalidateSecurityManifestCommandHandler revalidation) { return new DefaultCommandBus(List.of(registration, revalidation)); }
        @Bean("merchantPublication") SecurityManifestPublicationPort publication(MerchantSecurityManifestPublicationJpaRepository repository,
                @Qualifier("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> store) {
            var producer = new MerchantOutboxEventProducer(store, new JsonOutboxEventSerializer());
            return new SecurityManifestPublicationAdapter(repository::lockByModuleKey, producer,
                    envelope -> new MerchantSecurityManifestDeclaredIntegrationEvent(envelope.manifest(), envelope.eventId(), envelope.suppliedContentDigest(), envelope.publishedAt()),
                    Clock.systemUTC(), Duration.ofMinutes(5));
        }
    }

    @Configuration
    @EnableJpaRepositories(basePackages = "com.identity.adapter.persistence.repository.jpa", entityManagerFactoryRef = "identityEntityManagerFactory", transactionManagerRef = "identityTransactionManager")
    static class IdentityRepositories { }
    @Configuration
    @EnableJpaRepositories(basePackages = "com.merchant.adapter.persistence.repository.jpa", entityManagerFactoryRef = "merchantEntityManagerFactory", transactionManagerRef = "merchantTransactionManager")
    static class MerchantRepositories { }
}
