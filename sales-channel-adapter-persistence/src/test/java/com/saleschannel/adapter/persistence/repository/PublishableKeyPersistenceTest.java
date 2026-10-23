package com.saleschannel.adapter.persistence.repository;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.saleschannel.adapter.persistence.adapter.PublishableKeyQueryAdapter;
import com.saleschannel.adapter.persistence.adapter.PublishableKeyRepositoryAdapter;
import com.saleschannel.adapter.persistence.adapter.SalesChannelPersistenceExecutor;
import com.saleschannel.adapter.persistence.entity.PublishableKeyJpaEntity;
import com.saleschannel.adapter.persistence.entity.SalesChannelEntity;
import com.saleschannel.adapter.persistence.exception.SalesChannelInfraException;
import com.saleschannel.adapter.persistence.mapper.PublishableKeyEntityMapper;
import com.saleschannel.adapter.persistence.repository.config.PublishableKeyRepositoryTestConfig;
import com.saleschannel.adapter.persistence.repository.jpa.PublishableKeyJpaRepository;
import com.saleschannel.adapter.persistence.specification.jpa.PublishableKeyQuerySpecification;
import com.saleschannel.application.model.read.PublishableKeyView;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import com.saleschannel.domain.aggregate.PublishableKey;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.enums.ChannelType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.test.context.transaction.TestTransaction;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublishableKeyPersistenceTest extends PublishableKeyRepositoryTestConfig {
    @Autowired
    private EntityManagerFactory factory;
    private final Id channelId = new CommonId("channel-1");
    private final Instant now = Instant.parse("2026-10-23T00:00:00Z");
    private final RecordingEvents events = new RecordingEvents();
    private final SalesChannelPersistenceExecutor executor = new SalesChannelPersistenceExecutor();
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private PublishableKeyJpaRepository jpa;
    @Autowired
    private PublishableKeyEntityMapper mapper;
    private PublishableKeyRepositoryAdapter repository;
    private PublishableKeyQueryAdapter queries;

    @BeforeEach
    void setUp() {
        repository = new PublishableKeyRepositoryAdapter(jpa, mapper, events, executor);
        PublishableKeyQuerySpecification specification = new PublishableKeyQuerySpecification(entityManager);
        queries = new PublishableKeyQueryAdapter(specification, mapper, executor);
        persistChannel("channel-1", ChannelStatus.ENABLED);
        persistChannel("channel-2", ChannelStatus.ENABLED);
        entityManager.flush();
    }

    @Test
    void save_newKey_roundTripsAggregateAndPublishesLifecycleEvent() {
        PublishableKey key = key("key-1", channelId, "a", now);
        PublishableKey saved = repository.save(key);
        entityManager.clear();

        PublishableKey loaded = repository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getSalesChannelId()).isEqualTo(channelId);
        assertThat(loaded.getLabel()).isEqualTo("key-1");
        assertThat(loaded.getTokenDigest()).isEqualTo("a".repeat(64));
        assertThat(loaded.getDisplayPrefix()).isEqualTo("pk_aaaaaaaa");
        assertThat(loaded.getCreatedAt()).isEqualTo(now);
        assertThat(loaded.getEvents()).isEmpty();
        assertThat(key.getEvents()).isEmpty();
        assertThat(events.types).containsExactly("PublishableKey");
        assertThat(events.ids).containsExactly("key-1");
        assertThat(events.events).hasSize(1);
        assertThat(events.events.toString()).doesNotContain("a".repeat(64), "pk_aaaaaaaa");
        assertThat(repository.existsByDigest("a".repeat(64))).isTrue();
        PublishableKeyJpaEntity entity = jpa.findByUuid("key-1").orElseThrow();
        assertThat(entity.getId()).isPositive();
    }

    @Test
    void save_revocation_preservesImmutableFieldsAndIncrementsVersion() {
        PublishableKey key = key("key-1", channelId, "a", now);
        PublishableKey saved = repository.save(key);
        long initialVersion = jpa.findByUuid("key-1").orElseThrow().getVersion();
        Instant revokedAt = now.plusSeconds(60);
        saved.revoke(revokedAt);

        PublishableKey revoked = repository.save(saved);

        PublishableKeyJpaEntity stored = jpa.findByUuid("key-1").orElseThrow();
        assertThat(stored.getVersion()).isEqualTo(initialVersion + 1);
        assertThat(revoked.getRevokedAt()).isEqualTo(revokedAt);
        assertThat(revoked.getSalesChannelId()).isEqualTo(channelId);
        assertThat(revoked.getTokenDigest()).isEqualTo(saved.getTokenDigest());
        assertThat(events.events).hasSize(2);
        assertThat(queries.findActiveByDigest("a".repeat(64))).isEmpty();
    }

    @Test
    void save_staleAggregate_rejectsWithoutOverwritingOrPublishing() {
        PublishableKey original = key("key-1", channelId, "a", now);
        PublishableKey stale = repository.save(original);
        PublishableKey latest = repository.findById(stale.getId()).orElseThrow();
        Instant firstRevocation = now.plusSeconds(1);
        latest.revoke(firstRevocation);
        repository.save(latest);
        stale.revoke(now.plusSeconds(2));

        assertThatThrownBy(() -> repository.save(stale))
                .isInstanceOfSatisfying(SalesChannelInfraException.class,
                        exception -> assertThat(exception.getMessageSource().kind()).isEqualTo(ErrorCategory.CONFLICT));
        PublishableKey unchanged = repository.findById(stale.getId()).orElseThrow();
        assertThat(unchanged.getRevokedAt()).isEqualTo(firstRevocation);
        assertThat(events.events).hasSize(2);
        assertThat(stale.getEvents()).hasSize(1);
    }

    @Test
    void save_previouslyRevokedKey_cannotClearRevocation() {
        PublishableKey key = key("key-1", channelId, "a", now);
        key.revoke(now);
        PublishableKey saved = repository.save(key);
        PublishableKey activeCopy = new PublishableKey(saved.getId(), channelId, saved.getLabel(),
                saved.getTokenDigest(), saved.getDisplayPrefix(), now, null);

        assertThatThrownBy(() -> repository.save(activeCopy)).isInstanceOf(SalesChannelInfraException.class);
        PublishableKey loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.isActive()).isFalse();
    }

    @Test
    void save_concurrentTransactions_rejectsStaleJpaVersionWithoutDomainVersion() {
        EntityManager concurrentEntityManager = factory.createEntityManager();
        JpaRepositoryFactory concurrentFactory = new JpaRepositoryFactory(concurrentEntityManager);
        PublishableKeyJpaRepository concurrentJpa = concurrentFactory.getRepository(PublishableKeyJpaRepository.class);
        RecordingEvents committedEvents = new RecordingEvents();
        PublishableKeyRepositoryAdapter concurrentRepository =
                new PublishableKeyRepositoryAdapter(concurrentJpa, mapper, committedEvents, executor);
        Id concurrentKeyId = new CommonId("key-concurrent");
        Id concurrentChannelId = new CommonId("channel-concurrent");
        Instant firstRevocation = now.plusSeconds(1);
        try {
            concurrentEntityManager.getTransaction().begin();
            PublishableKey key = key("key-concurrent", concurrentChannelId, "d", now);
            concurrentRepository.save(key);
            concurrentEntityManager.getTransaction().commit();

            PublishableKey stale = repository.findById(concurrentKeyId).orElseThrow();
            concurrentEntityManager.getTransaction().begin();
            PublishableKey latest = concurrentRepository.findById(concurrentKeyId).orElseThrow();
            latest.revoke(firstRevocation);
            concurrentRepository.save(latest);
            concurrentEntityManager.getTransaction().commit();
            stale.revoke(now.plusSeconds(2));

            assertThatThrownBy(() -> repository.save(stale))
                    .isInstanceOfSatisfying(SalesChannelInfraException.class,
                            exception -> assertThat(exception.getMessageSource().kind()).isEqualTo(ErrorCategory.CONFLICT));
            assertThat(events.events).isEmpty();
            assertThat(committedEvents.events).hasSize(2);
            TestTransaction.flagForRollback();
            TestTransaction.end();
            TestTransaction.start();
            PublishableKey unchanged = repository.findById(concurrentKeyId).orElseThrow();
            assertThat(unchanged.getRevokedAt()).isEqualTo(firstRevocation);
        } finally {
            if (TestTransaction.isActive()) {
                TestTransaction.flagForRollback();
                TestTransaction.end();
            }
            if (concurrentEntityManager.getTransaction().isActive()) {
                concurrentEntityManager.getTransaction().rollback();
            }
            concurrentEntityManager.clear();
            concurrentEntityManager.getTransaction().begin();
            concurrentEntityManager.createQuery("delete from PublishableKeyJpaEntity where uuid = :uuid")
                    .setParameter("uuid", "key-concurrent")
                    .executeUpdate();
            concurrentEntityManager.getTransaction().commit();
            concurrentEntityManager.close();
        }
    }

    @Test
    void save_changedChannelAssociation_rejectsWithoutMutatingStoredIdentity() {
        PublishableKey saved = repository.save(key("key-1", channelId, "a", now));
        Id otherChannel = new CommonId("channel-2");
        PublishableKey changed = new PublishableKey(saved.getId(), otherChannel, saved.getLabel(),
                saved.getTokenDigest(), saved.getDisplayPrefix(), now, null);

        assertThatThrownBy(() -> repository.save(changed)).isInstanceOf(SalesChannelInfraException.class);
        PublishableKey loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getSalesChannelId()).isEqualTo(channelId);
        assertThat(events.events).hasSize(1);
    }

    @Test
    void save_duplicateDigest_databaseRejectsAndDoesNotPublishSecondEvent() {
        repository.save(key("key-1", channelId, "a", now));
        PublishableKey duplicate = key("key-2", channelId, "a", now);

        assertThatThrownBy(() -> repository.save(duplicate))
                .isInstanceOfSatisfying(SalesChannelInfraException.class,
                        exception -> assertThat(exception.getMessageSource().kind()).isEqualTo(ErrorCategory.CONFLICT));
        assertThat(events.events).hasSize(1);
        assertThat(duplicate.getEvents()).hasSize(1);
    }

    @Test
    void list_mixedChannelsAndRevocations_returnsStableMetadataPagesOnly() {
        repository.save(key("key-a", channelId, "a", now));
        PublishableKey revoked = key("key-b", channelId, "b", now);
        revoked.revoke(now.plusSeconds(1));
        repository.save(revoked);
        Id otherChannel = new CommonId("channel-2");
        repository.save(key("key-c", otherChannel, "c", now.plusSeconds(2)));

        Page<PublishableKeyView> first = queries.list(channelId, PageRequest.of(0, 1));
        Page<PublishableKeyView> second = queries.list(channelId, PageRequest.of(1, 1));

        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(first.getContent()).extracting(view -> view.keyId().getValue()).containsExactly("key-a");
        assertThat(second.getContent()).extracting(view -> view.keyId().getValue()).containsExactly("key-b");
        assertThat(second.getContent().getFirst().revokedAt()).isEqualTo(now.plusSeconds(1));
        RecordComponent[] components = PublishableKeyView.class.getRecordComponents();
        List<String> fields = Arrays.stream(components).map(RecordComponent::getName).toList();
        assertThat(fields).doesNotContain("rawToken", "tokenDigest", "digest");
    }

    @Test
    void list_sortAndUnpaged_appliesRequestedOrderAndChannelScope() {
        repository.save(key("key-a", channelId, "a", now));
        repository.save(key("key-b", channelId, "b", now.plusSeconds(1)));
        Sort sort = Sort.by(Sort.Direction.DESC, "label");
        PageRequest pageable = PageRequest.of(0, 20, sort);
        Page<PublishableKeyView> page = queries.list(channelId, pageable);
        assertThat(page.getContent()).extracting(view -> view.keyId().getValue())
                .containsExactly("key-b", "key-a");
        assertThat(queries.list(channelId, Pageable.unpaged()).getTotalElements()).isEqualTo(2);
        Id missing = new CommonId("missing");
        assertThat(queries.list(missing, pageable)).isEmpty();
    }

    @Test
    void list_tokenMaterialSort_rejectsUnsupportedField() {
        Sort sort = Sort.by("tokenDigest");
        PageRequest pageable = PageRequest.of(0, 20, sort);
        assertThatThrownBy(() -> queries.list(channelId, pageable)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findActiveByDigest_missingRevokedOrOrphanedKey_returnsEmpty() {
        PublishableKey revoked = key("key-revoked", channelId, "a", now);
        revoked.revoke(now);
        repository.save(revoked);
        Id missingChannel = new CommonId("missing");
        repository.save(key("key-orphan", missingChannel, "b", now));

        assertThat(queries.findActiveByDigest("a".repeat(64))).isEmpty();
        assertThat(queries.findActiveByDigest("b".repeat(64))).isEmpty();
        assertThat(queries.findActiveByDigest("c".repeat(64))).isEmpty();
    }

    @Test
    void findActiveByDigest_disabledChannel_preservesDisabledStatus() {
        SalesChannelEntity channel = findChannel("channel-1");
        channel.setStatus(ChannelStatus.DISABLED);
        repository.save(key("key-1", channelId, "a", now));

        Optional<ResolvedPublishableKeyView> resolved = queries.findActiveByDigest("a".repeat(64));

        assertThat(resolved).contains(new ResolvedPublishableKeyView(channelId, false, true));
    }

    @Test
    void findActiveByDigest_enabledChannel_returnsActiveMetadata() {
        repository.save(key("key-1", channelId, "a", now));
        assertThat(queries.findActiveByDigest("a".repeat(64)))
                .contains(new ResolvedPublishableKeyView(channelId, true, true));
    }

    @Test
    void save_eventProducerFailure_callerTransactionCanRollBackKeyWrite() {
        DomainEventProducer failingEvents = (type, id, pending) -> {
            throw new IllegalStateException("Outbox unavailable");
        };
        PublishableKeyRepositoryAdapter failingRepository =
                new PublishableKeyRepositoryAdapter(jpa, mapper, failingEvents, executor);
        PublishableKey key = key("key-1", channelId, "a", now);

        assertThatThrownBy(() -> failingRepository.save(key)).isInstanceOf(IllegalStateException.class);
        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        assertThat(repository.findById(key.getId())).isEmpty();
    }

    private PublishableKey key(String id, Id salesChannelId, String digestCharacter, Instant createdAt) {
        Id publicId = new CommonId(id);
        String digest = digestCharacter.repeat(64);
        return PublishableKey.create(publicId, salesChannelId, id, digest, "pk_aaaaaaaa", createdAt);
    }

    private void persistChannel(String id, ChannelStatus status) {
        SalesChannelEntity channel = new SalesChannelEntity();
        channel.setUuid(id);
        channel.setName(id);
        channel.setType(ChannelType.WEBSITE);
        channel.setOwner(ChannelOwner.SELLER);
        channel.setMerchantId("merchant-1");
        channel.setStatus(status);
        channel.setCreatedAt(now);
        channel.setUpdatedAt(now);
        entityManager.persist(channel);
    }

    private SalesChannelEntity findChannel(String uuid) {
        return entityManager.createQuery("select c from SalesChannelEntity c where c.uuid = :uuid",
                        SalesChannelEntity.class)
                .setParameter("uuid", uuid)
                .getSingleResult();
    }

    private static class RecordingEvents implements DomainEventProducer {
        private final List<Event> events = new ArrayList<>();
        private final List<String> types = new ArrayList<>();
        private final List<String> ids = new ArrayList<>();

        @Override
        public void produce(String aggregateType, String aggregateId, List<Event> pending) {
            if (!pending.isEmpty()) {
                types.add(aggregateType);
                ids.add(aggregateId);
                events.addAll(pending);
            }
        }
    }
}
