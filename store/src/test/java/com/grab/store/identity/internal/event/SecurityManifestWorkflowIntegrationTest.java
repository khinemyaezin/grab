package com.grab.store.identity.internal.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.cqrs.command.impl.DefaultCommandBus;
import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.impl.DefaultQueryBus;
import com.grab.store.identity.port.IdentityLookupQuery;
import com.grab.store.identity.internal.query.handler.ListWaitingSecurityManifestCandidatesQueryHandler;
import com.grab.store.identity.internal.config.SecurityCatalogInitializationAspect;
import com.identity.adapter.persistence.specification.jpa.SecurityManifestWaitingSpecification;
import com.identity.adapter.persistence.mapper.jpa.SecurityCatalogJpaAssembler;
import com.identity.application.port.outbound.SecurityManifestQueryPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import com.grab.framework.outbox.*;
import com.grab.framework.security.*;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import com.grab.store.identity.internal.command.handler.RegisterSecurityManifestCommandHandler;
import com.grab.store.identity.internal.command.handler.RevalidateSecurityManifestCommandHandler;
import com.grab.store.identity.internal.command.handler.EnsureSecurityCatalogStateCommandHandler;
import com.grab.store.identity.internal.command.handler.RegisterRoleDeclarationCommandHandler;
import com.grab.store.identity.internal.command.handler.GrantAccessCommandHandler;
import com.grab.store.identity.internal.command.handler.ReplaceAccessCommandHandler;
import com.grab.store.identity.internal.command.handler.ChangeAccessStatusCommandHandler;
import com.grab.store.identity.internal.command.handler.CreateAccessInvitationCommandHandler;
import com.grab.store.identity.internal.command.handler.AcceptAccessInvitationCommandHandler;
import com.grab.store.identity.internal.command.handler.CancelAccessInvitationCommandHandler;
import com.grab.store.identity.internal.api.adapter.AccessManagementPortAdapter;
import com.grab.store.identity.port.AccessManagementPort;
import com.grab.store.merchant.internal.command.handler.PublishMerchantSecurityManifestCommandHandler;
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
import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;
import com.merchant.application.port.inbound.PublishMerchantSecurityManifestUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

@SpringJUnitConfig(SecurityManifestWorkflowIntegrationTest.Config.class)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SecurityManifestWorkflowIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private RegisterSecurityManifestCommandHandler registration;
    @Autowired private RevalidateSecurityManifestCommandHandler revalidation;
    @Autowired private RegisterRoleDeclarationCommandHandler roleRegistration;
    @Autowired private GrantAccessCommandHandler grantAccess;
    @Autowired private ReplaceAccessCommandHandler replaceAccess;
    @Autowired private ChangeAccessStatusCommandHandler changeAccessStatus;
    @Autowired private CreateAccessInvitationCommandHandler createInvitation;
    @Autowired private AcceptAccessInvitationCommandHandler acceptInvitation;
    @Autowired private CancelAccessInvitationCommandHandler cancelInvitation;
    @Autowired private AccessManagementPortAdapter accessManagement;
    @Autowired private InitializationRaceControl initializationRaceControl;
    @Autowired private SecurityManifestRevisionRepository revisions;
    @Autowired private SecurityCatalogRepository catalogs;
    @Autowired private CommandBus commands;
    @Autowired private PublicationResultCapture publicationResultCapture;
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
                + "security_manifest_module, security_catalog_state, security_scope_definitions, role_authorities, authorities, identity_outbox_event, roles, users, access_assignments CASCADE");
        merchant.execute("TRUNCATE merchant_outbox_events");
        merchant.execute("TRUNCATE security_manifest_publication");
    }

    @Test
    void publish_rollbackAndCrashGap_preservesOnlyCommittedEnqueue() {
        var manifest = merchant(2, Lifecycle.ACTIVE);
        var tx = new TransactionTemplate(merchantTransactions);
        assertThatThrownBy(() -> tx.execute(status -> {
            publication.enqueue(manifest);
            throw new IllegalStateException("injected owner crash");
        })).isInstanceOf(IllegalStateException.class);
        Long outboxCountAfterRollback = merchant.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class);
        Long stateCountAfterRollback = merchant.queryForObject("SELECT count(*) FROM security_manifest_publication", Long.class);
        assertThat(outboxCountAfterRollback).isZero();
        assertThat(stateCountAfterRollback).isZero();
        tx.executeWithoutResult(status -> publication.enqueue(manifest));
        Long outboxCountAfterCommit = merchant.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class);
        assertThat(outboxCountAfterCommit).isEqualTo(1);
        var listener = new IdentitySecurityManifestRegistrationListener(commands);
        var relay = new Relay<>(merchantOutbox, new JsonOutboxEventSerializer(),
                event -> listener.onSecurityManifestDeclared((SecurityManifestDeclaredIntegrationEvent) event), merchantTransactions);
        relay.process();
        assertThat(applied("merchant")).isEqualTo(2);
        String outboxStatus = merchant.queryForObject("SELECT status FROM merchant_outbox_events", String.class);
        assertThat(outboxStatus).isEqualTo("PUBLISHED");
    }

    @Test
    void publish_concurrentReplicasAndOldBinary_enqueueOnceWithoutDowngrade() throws Exception {
        var manifest = merchant(3, Lifecycle.ACTIVE);
        var ready = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<SecurityManifestPublicationPort.PublicationResult> worker = () -> {
                ready.await();
                publicationResultCapture.setManifest(manifest);
                try {
                    commands.dispatch(new PublishMerchantSecurityManifestCommand());
                    return publicationResultCapture.take();
                } finally {
                    publicationResultCapture.clear();
                }
            };
            var first = pool.submit(worker);
            var second = pool.submit(worker);
            ready.countDown();
            var firstResult = first.get(10, TimeUnit.SECONDS);
            var secondResult = second.get(10, TimeUnit.SECONDS);
            assertThat(List.of(firstResult, secondResult))
                    .containsExactlyInAnyOrder(SecurityManifestPublicationPort.PublicationResult.ENQUEUED,
                            SecurityManifestPublicationPort.PublicationResult.NOT_DUE);
        }
        merchant.update("UPDATE security_manifest_publication SET lease_until = CURRENT_TIMESTAMP - INTERVAL '1 minute'");
        var tx = new TransactionTemplate(merchantTransactions);
        var olderManifest = merchant(2, Lifecycle.ACTIVE);
        var result = tx.execute(status -> publication.enqueue(olderManifest));
        assertThat(result).isEqualTo(SecurityManifestPublicationPort.PublicationResult.SUPERSEDED);
        Integer revision = merchant.queryForObject("SELECT security_revision FROM security_manifest_publication", Integer.class);
        Long eventCount = merchant.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class);
        assertThat(revision).isEqualTo(3);
        assertThat(eventCount).isEqualTo(1);
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
            assertThat(identity.queryForObject("SELECT count(*) FROM security_catalog_state", Long.class)).isEqualTo(1L);
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
        IdentityRoleDeclarationRevalidationScheduler roleRevalidation =
                new IdentityRoleDeclarationRevalidationScheduler(null, null, 100) {
                    @Override
                    public void revalidate() {
                    }
                };
        IdentitySecurityCatalogActivationListener activation =
                new IdentitySecurityCatalogActivationListener(event -> { }, scheduler, roleRevalidation);
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
    void register_catalogLock_contentionWaitsForHolderAndThenActivates() throws Exception {
        EnsureSecurityCatalogStateResult initialized = commands.dispatch(new EnsureSecurityCatalogStateCommand(true));
        assertThat(initialized.initialized()).isTrue();
        try (var pool = Executors.newFixedThreadPool(2)) {
            var locked = new CountDownLatch(1);
            var release = new CountDownLatch(1);
            var holder = pool.submit(() -> new TransactionTemplate(identityTransactions).executeWithoutResult(status -> {
                catalogs.loadForUpdate();
                locked.countDown();
                try { release.await(10, TimeUnit.SECONDS); } catch (InterruptedException ex) { throw new IllegalStateException(ex); }
            }));
            assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();
            var contender = pool.submit(() -> register(merchant(2, Lifecycle.ACTIVE), "contended"));
            try {
                String lockWaitQuery = "SELECT count(*) FROM pg_stat_activity WHERE datname = current_database() "
                        + "AND pid <> pg_backend_pid() AND wait_event_type = 'Lock'";
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                Long waiting = 0L;
                while (waiting == 0L && System.nanoTime() < deadline) {
                    waiting = identity.queryForObject(lockWaitQuery, Long.class);
                    Thread.onSpinWait();
                }
                assertThat(waiting).isPositive();
                assertThat(contender.isDone()).isFalse();
            } finally {
                release.countDown();
            }
            holder.get(10, TimeUnit.SECONDS);
            assertThat(contender.get(10, TimeUnit.SECONDS).newlyActivated()).isTrue();
        }
        assertThat(identity.queryForObject("SELECT catalog_revision FROM security_catalog_state WHERE id = 1", Long.class)).isEqualTo(1L);
    }

    @Test
    void register_concurrentFirstManifests_initializesOnceAndAppliesBoth() throws Exception {
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var merchantRegistration = pool.submit(() -> {
                start.await();
                return register(merchant(2, Lifecycle.ACTIVE), "first-merchant");
            });
            var catalogRegistration = pool.submit(() -> {
                start.await();
                return register(catalog(2, Lifecycle.ACTIVE), "first-catalog");
            });
            start.countDown();
            assertThat(merchantRegistration.get(10, TimeUnit.SECONDS).newlyActivated()).isTrue();
            assertThat(catalogRegistration.get(10, TimeUnit.SECONDS).newlyActivated()).isTrue();
        }

        assertThat(identity.queryForObject("SELECT count(*) FROM security_catalog_state", Long.class)).isEqualTo(1L);
        assertThat(identity.queryForObject("SELECT catalog_revision FROM security_catalog_state WHERE id = 1", Long.class)).isEqualTo(2L);
        assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_module WHERE applied_revision = 2", Long.class)).isEqualTo(2L);
        assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_revision WHERE status = 'APPLIED'", Long.class)).isEqualTo(2L);
        assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_inbox", Long.class)).isEqualTo(2L);
        assertThat(identity.queryForObject("SELECT count(*) FROM identity_outbox_event", Long.class)).isEqualTo(2L);
    }

    @Test
    void ensureCatalog_emptyDatabaseInitializesWithoutRevisionOrVersionLoss() {
        EnsureSecurityCatalogStateResult created = commands.dispatch(new EnsureSecurityCatalogStateCommand(true));
        assertThat(created.initialized()).isTrue();
        assertThat(initializationRaceControl.identityTransactionActive()).isTrue();
        assertThat(initializationRaceControl.merchantTransactionActive()).isFalse();
        assertThat(identity.queryForMap("SELECT catalog_revision, row_version FROM security_catalog_state WHERE id = 1"))
                .containsEntry("catalog_revision", 0L)
                .containsEntry("row_version", 0L);

        identity.update("UPDATE security_catalog_state SET catalog_revision = 7, row_version = 4 WHERE id = 1");
        EnsureSecurityCatalogStateResult preserved = commands.dispatch(new EnsureSecurityCatalogStateCommand(true));
        assertThat(preserved.initialized()).isTrue();
        assertThat(identity.queryForMap("SELECT catalog_revision, row_version FROM security_catalog_state WHERE id = 1"))
                .containsEntry("catalog_revision", 7L)
                .containsEntry("row_version", 4L);
    }

    @Test
    void ensureCatalog_duplicatePrimaryKeyRace_isVerifiedAfterRollback() throws Exception {
        initializationRaceControl.arm(2);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var start = new CountDownLatch(1);
            var first = pool.submit(() -> {
                start.await();
                return register(merchant(2, Lifecycle.ACTIVE), "race-merchant");
            });
            var second = pool.submit(() -> {
                start.await();
                return register(catalog(2, Lifecycle.ACTIVE), "race-catalog");
            });
            start.countDown();
            assertThat(initializationRaceControl.awaitingBoth(10, TimeUnit.SECONDS)).isTrue();
            initializationRaceControl.release();
            assertThat(first.get(10, TimeUnit.SECONDS).newlyActivated()).isTrue();
            assertThat(second.get(10, TimeUnit.SECONDS).newlyActivated()).isTrue();
        }
        assertThat(identity.queryForObject("SELECT count(*) FROM security_catalog_state", Long.class)).isEqualTo(1L);
        assertThat(identity.queryForObject("SELECT catalog_revision FROM security_catalog_state WHERE id = 1", Long.class)).isEqualTo(2L);
        assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_revision WHERE status = 'APPLIED'", Long.class)).isEqualTo(2L);
    }

    @Test
    void getCatalogStatus_emptyDatabaseReturnsZeroWithoutInitializing() {
        var view = manifestQueries.status("merchant");
        assertThat(view.catalogRevision()).isZero();
        assertThat(identity.queryForObject("SELECT count(*) FROM security_catalog_state", Long.class)).isZero();
    }

    @Test
    void catalogDependentWriteEntryPoints_emptyCatalogInitializeBeforeBusinessFailure() {
        List<Runnable> entryPoints = List.of(
                () -> registration.handle(null),
                () -> revalidation.handle(null),
                () -> roleRegistration.handle(null),
                () -> grantAccess.handle(null),
                () -> replaceAccess.handle(null),
                () -> changeAccessStatus.handle(null),
                () -> createInvitation.handle(null),
                () -> acceptInvitation.handle(null),
                () -> cancelInvitation.handle(null),
                () -> accessManagement.replaceAccess(new AccessManagementPort.ReplaceAccessRequest(
                        "user-1", "OLD_ROLE", "NEW_ROLE", "merchant.account", "merchant-1"))
        );

        for (Runnable entryPoint : entryPoints) {
            assertThatThrownBy(entryPoint::run).isInstanceOf(RuntimeException.class);
            assertThat(identity.queryForObject("SELECT count(*) FROM security_catalog_state WHERE id = 1", Long.class))
                    .isEqualTo(1L);
            identity.execute("TRUNCATE security_catalog_state");
        }
    }

    @Test
    void ensureCatalog_unrelatedConstraintFailurePropagatesWithoutBusinessWrites() {
        identity.execute("ALTER TABLE security_catalog_state ADD CONSTRAINT ck_catalog_revision_positive CHECK (catalog_revision > 0)");
        try {
            assertThatThrownBy(() -> register(merchant(2, Lifecycle.ACTIVE), "invalid-initial-row"))
                    .isInstanceOf(RuntimeException.class);
            assertThat(identity.queryForObject("SELECT count(*) FROM security_catalog_state", Long.class)).isZero();
            assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_inbox", Long.class)).isZero();
            assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_revision", Long.class)).isZero();
        } finally {
            identity.execute("ALTER TABLE security_catalog_state DROP CONSTRAINT ck_catalog_revision_positive");
        }
    }

    @Test
    void register_lockTimeoutPropagatesWithoutPartialActivation() throws Exception {
        String databaseName = identity.queryForObject("SELECT current_database()", String.class);
        String quotedDatabaseName = "\"" + databaseName.replace("\"", "\"\"") + "\"";
        identity.execute("ALTER DATABASE " + quotedDatabaseName + " SET lock_timeout TO '200ms'");
        EnsureSecurityCatalogStateResult initialized = commands.dispatch(new EnsureSecurityCatalogStateCommand(true));
        assertThat(initialized.initialized()).isTrue();

        try (var pool = Executors.newFixedThreadPool(2)) {
            var locked = new CountDownLatch(1);
            var release = new CountDownLatch(1);
            var holder = pool.submit(() -> new TransactionTemplate(identityTransactions).executeWithoutResult(status -> {
                catalogs.loadForUpdate();
                locked.countDown();
                try { release.await(10, TimeUnit.SECONDS); } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
            }));
            assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();
            var contender = pool.submit(() -> register(merchant(2, Lifecycle.ACTIVE), "lock-timeout"));
            try {
                assertThatThrownBy(() -> contender.get(5, TimeUnit.SECONDS))
                        .isInstanceOf(ExecutionException.class);
                assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_inbox", Long.class)).isZero();
                assertThat(identity.queryForObject("SELECT count(*) FROM security_manifest_revision", Long.class)).isZero();
                assertThat(identity.queryForObject("SELECT catalog_revision FROM security_catalog_state WHERE id = 1", Long.class)).isZero();
            } finally {
                release.countDown();
            }
            holder.get(10, TimeUnit.SECONDS);
        } finally {
            identity.execute("ALTER DATABASE " + quotedDatabaseName + " RESET lock_timeout");
        }
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

    private SecurityManifest catalog(int revision, Lifecycle lifecycle) {
        return new SecurityManifest("catalog", revision,
                List.of(new ScopeDeclaration("catalog.product", null, lifecycle)),
                List.of(new AuthorityDefinition("CATALOG_READ", "Read", null, "catalog", lifecycle)));
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
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @EnableRetry(proxyTargetClass = true, order = Ordered.HIGHEST_PRECEDENCE)
    @Import({IdentityRepositories.class, MerchantRepositories.class, SecurityCatalogInitializationAspect.class})
    static class Config {
        @Bean("identityDataSource") DataSource identityDataSource() { return database("identity"); }
        @Bean("merchantDataSource") DataSource merchantDataSource() { return database("merchant"); }
        private DataSource database(String schema) {
            String jdbcUrl = DATABASE.getJdbcUrl();
            String separator = jdbcUrl.contains("?") ? "&" : "?";
            var admin = new DriverManagerDataSource(jdbcUrl, DATABASE.getUsername(), DATABASE.getPassword());
            new JdbcTemplate(admin).execute("CREATE SCHEMA IF NOT EXISTS " + schema);
            var source = new DriverManagerDataSource(jdbcUrl + separator + "currentSchema=" + schema, DATABASE.getUsername(), DATABASE.getPassword());
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
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
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
        @Bean InitializationRaceControl initializationRaceControl() {
            return new InitializationRaceControl();
        }
        @Bean EnsureSecurityCatalogStateUseCase ensureSecurityCatalogStateUseCase(
                SecurityCatalogRepository catalogs,
                InitializationRaceControl raceControl,
                @Qualifier("identityEntityManagerFactory") EntityManagerFactory identityFactory,
                @Qualifier("merchantEntityManagerFactory") EntityManagerFactory merchantFactory
        ) {
            return command -> {
                raceControl.recordTransactionManagers(
                        TransactionSynchronizationManager.hasResource(identityFactory),
                        TransactionSynchronizationManager.hasResource(merchantFactory)
                );
                if (command.creationAllowed() && !catalogs.ensureInitialized(false)) {
                    raceControl.awaitIfArmed();
                }
                boolean initialized = catalogs.ensureInitialized(command.creationAllowed());
                return new EnsureSecurityCatalogStateResult(initialized);
            };
        }
        @Bean EnsureSecurityCatalogStateCommandHandler ensureSecurityCatalogStateCommandHandler(EnsureSecurityCatalogStateUseCase useCase) {
            return new EnsureSecurityCatalogStateCommandHandler(useCase);
        }
        @Bean SecurityManifestRevisionRepository revisions(SecurityManifestRevisionJpaRepository repository, ObjectMapper mapper) { return new SecurityManifestRevisionRepositoryAdapter(repository, mapper); }
        @Bean SecurityManifestInboxRepository inbox(SecurityManifestInboxJpaRepository repository, SecurityManifestConflictJpaRepository conflicts, ObjectMapper mapper) { return new SecurityManifestInboxRepositoryAdapter(repository, conflicts, mapper); }
        @Bean RegisterSecurityManifestUseCase registration(SecurityCatalogRepository catalogs, SecurityManifestRevisionRepository revisions, SecurityManifestInboxRepository inbox) { return new RegisterSecurityManifestService(catalogs, revisions, inbox); }
        @Bean RevalidateSecurityManifestUseCase revalidation(SecurityCatalogRepository catalogs, SecurityManifestRevisionRepository revisions, RegisterSecurityManifestUseCase registration) { return new RevalidateSecurityManifestService(catalogs, revisions, registration); }
        @Bean RegisterSecurityManifestCommandHandler registrationHandler(RegisterSecurityManifestUseCase registration) { return new RegisterSecurityManifestCommandHandler(registration); }
        @Bean RevalidateSecurityManifestCommandHandler revalidationHandler(RevalidateSecurityManifestUseCase revalidation) { return new RevalidateSecurityManifestCommandHandler(revalidation); }
        @Bean RegisterRoleDeclarationCommandHandler roleRegistrationHandler() { return new RegisterRoleDeclarationCommandHandler(null); }
        @Bean GrantAccessCommandHandler grantAccessHandler() { return new GrantAccessCommandHandler(null); }
        @Bean ReplaceAccessCommandHandler replaceAccessHandler() { return new ReplaceAccessCommandHandler(null); }
        @Bean ChangeAccessStatusCommandHandler changeAccessStatusHandler() { return new ChangeAccessStatusCommandHandler(null); }
        @Bean CreateAccessInvitationCommandHandler createInvitationHandler() { return new CreateAccessInvitationCommandHandler(null); }
        @Bean AcceptAccessInvitationCommandHandler acceptInvitationHandler() { return new AcceptAccessInvitationCommandHandler(null); }
        @Bean CancelAccessInvitationCommandHandler cancelInvitationHandler() { return new CancelAccessInvitationCommandHandler(null); }
        @Bean AccessManagementPortAdapter accessManagementAdapter(IdGenerator ids) { return new AccessManagementPortAdapter(null, null, ids); }
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
        @Bean CommandBus commands(RegisterSecurityManifestCommandHandler registration,
                RevalidateSecurityManifestCommandHandler revalidation,
                EnsureSecurityCatalogStateCommandHandler ensureSecurityCatalogState,
                PublishMerchantSecurityManifestCommandHandler merchantPublication) {
            List<CommandHandler<?, ?>> handlers = List.of(registration, revalidation, ensureSecurityCatalogState, merchantPublication);
            return new DefaultCommandBus(handlers);
        }
        @Bean PublicationResultCapture publicationResultCapture() { return new PublicationResultCapture(); }
        @Bean PublishMerchantSecurityManifestUseCase merchantPublicationUseCase(
                @Qualifier("merchantPublication") SecurityManifestPublicationPort publication,
                PublicationResultCapture resultCapture) {
            return command -> {
                SecurityManifest manifest = resultCapture.manifest();
                SecurityManifestPublicationPort.PublicationResult result = publication.enqueue(manifest);
                resultCapture.set(result);
            };
        }
        @Bean PublishMerchantSecurityManifestCommandHandler merchantPublicationHandler(
                PublishMerchantSecurityManifestUseCase useCase) {
            return new PublishMerchantSecurityManifestCommandHandler(useCase);
        }
        @Bean("merchantPublication") SecurityManifestPublicationPort publication(
                @Qualifier("merchantEntityManagerFactory") EntityManagerFactory factory,
                @Qualifier("merchantOutboxStore") OutboxStore<MerchantOutboxEvent, Long> store) {
            JsonOutboxEventSerializer serializer = new JsonOutboxEventSerializer();
            var producer = new MerchantOutboxEventProducer(store, serializer);
            EntityManager entityManager = SharedEntityManagerCreator.createSharedEntityManager(factory);
            var stateProvider = SecurityManifestPublicationStateProvider.optimisticProvider(
                    entityManager, MerchantSecurityManifestPublicationEntity.class, MerchantSecurityManifestPublicationEntity::new);
            Function<SecurityManifestEnvelope, Event> eventFactory = envelope ->
                    new MerchantSecurityManifestDeclaredIntegrationEvent(envelope.manifest(), envelope.eventId(),
                            envelope.suppliedContentDigest(), envelope.publishedAt());
            Clock clock = Clock.systemUTC();
            Duration publicationInterval = Duration.ofMinutes(5);
            return new SecurityManifestPublicationAdapter(stateProvider, producer, eventFactory, clock, publicationInterval);
        }
    }

    static class PublicationResultCapture {
        private final ThreadLocal<SecurityManifestPublicationPort.PublicationResult> result = new ThreadLocal<>();
        private final ThreadLocal<SecurityManifest> manifest = new ThreadLocal<>();

        void setManifest(SecurityManifest value) {
            manifest.set(value);
        }

        SecurityManifest manifest() {
            return manifest.get();
        }

        void set(SecurityManifestPublicationPort.PublicationResult publicationResult) {
            result.set(publicationResult);
        }

        SecurityManifestPublicationPort.PublicationResult take() {
            SecurityManifestPublicationPort.PublicationResult publicationResult = result.get();
            result.remove();
            return publicationResult;
        }

        void clear() {
            result.remove();
            manifest.remove();
        }
    }

    static class InitializationRaceControl {
        private volatile CyclicBarrier barrier;
        private volatile CountDownLatch bothWaiting = new CountDownLatch(0);
        private volatile boolean identityTransactionActive;
        private volatile boolean merchantTransactionActive;

        void recordTransactionManagers(boolean identityActive, boolean merchantActive) {
            identityTransactionActive = identityActive;
            merchantTransactionActive = merchantActive;
        }

        boolean identityTransactionActive() {
            return identityTransactionActive;
        }

        boolean merchantTransactionActive() {
            return merchantTransactionActive;
        }

        void arm(int parties) {
            bothWaiting = new CountDownLatch(parties);
            barrier = new CyclicBarrier(parties);
        }

        void awaitIfArmed() {
            CyclicBarrier currentBarrier = barrier;
            if (currentBarrier == null) {
                return;
            }
            bothWaiting.countDown();
            try {
                currentBarrier.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            } catch (BrokenBarrierException | TimeoutException exception) {
                throw new IllegalStateException(exception);
            }
        }

        boolean awaitingBoth(long timeout, TimeUnit unit) throws InterruptedException {
            return bothWaiting.await(timeout, unit);
        }

        void release() {
            barrier = null;
        }
    }

    @Configuration
    @EnableJpaRepositories(basePackages = "com.identity.adapter.persistence.repository.jpa", entityManagerFactoryRef = "identityEntityManagerFactory", transactionManagerRef = "identityTransactionManager")
    static class IdentityRepositories { }
    @Configuration
    @EnableJpaRepositories(basePackages = "com.merchant.adapter.persistence.repository.jpa", entityManagerFactoryRef = "merchantEntityManagerFactory", transactionManagerRef = "merchantTransactionManager")
    static class MerchantRepositories { }
}
