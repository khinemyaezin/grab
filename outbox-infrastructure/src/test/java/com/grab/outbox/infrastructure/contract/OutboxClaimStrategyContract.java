package com.grab.outbox.infrastructure.contract;

import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxEventEntity;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.adapter.out.jpa.OutboxRowFactory;
import com.grab.outbox.infrastructure.adapter.out.jpa.claim.OutboxClaimStrategy;
import com.grab.outbox.infrastructure.core.port.out.OutboxClaimRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behaviour every {@link OutboxClaimStrategy} must provide, on every database it is configured for. Runs without a
 * test-managed transaction: each claim gets its own short transaction, like the processor's claim template.
 */
@DataJpaTest(excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public abstract class OutboxClaimStrategyContract<T extends JpaOutboxEventEntity> {

    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");
    private static final Duration CLAIM_TIMEOUT = Duration.ofMinutes(2);
    private static final List<OutboxStatus> RETRYABLE = List.of(OutboxStatus.NEW, OutboxStatus.FAILED);

    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager transactionManager;

    protected TransactionTemplate tx;
    protected JpaOutboxStore<T, Long> store;

    protected abstract OutboxClaimStrategy strategy();

    protected abstract Class<T> entityType();

    protected abstract OutboxRowFactory<T> rowFactory();

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(transactionManager);
        store = new JpaOutboxStore<>(entityManager, entityType(), strategy());
        String entity = entityManager.getMetamodel().entity(entityType()).getName();
        tx.executeWithoutResult(status -> entityManager.createQuery("delete from " + entity).executeUpdate());
    }

    @Test
    void claimBatch_claimsOnlyReadyRowsAndMarksThemProcessing() {
        Long ready = insert(event -> { });
        Long retryable = insert(event -> {
            event.markProcessing(NOW.minusSeconds(60), "old");
            event.markFailed("boom", NOW.minusSeconds(1));
        });
        Long notYetAvailable = insert(event -> event.markFailed("boom", NOW.plusSeconds(30)));
        Long published = insert(event -> event.markPublished(NOW));
        Long dead = insert(event -> event.markDead(NOW, "boom"));

        List<ClaimedOutboxEvent<Long>> claimed = claimBatch(10);

        assertThat(claimed).extracting(ClaimedOutboxEvent::id).containsExactlyInAnyOrder(ready, retryable);
        for (ClaimedOutboxEvent<Long> claim : claimed) {
            T row = reload(claim.id());
            assertThat(row.getStatus()).isEqualTo(OutboxStatus.PROCESSING);
            assertThat(row.getClaimToken()).isEqualTo(claim.claimToken());
            assertThat(row.getClaimedAt()).isEqualTo(NOW);
            assertThat(row.isClaimedBy(claim.claimToken())).isTrue();
        }
        assertThat(reload(ready).getAttemptCount()).isEqualTo(1);
        assertThat(reload(notYetAvailable).getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(reload(published).getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(reload(dead).getStatus()).isEqualTo(OutboxStatus.DEAD);
    }

    @Test
    void claimBatch_respectsLimit() {
        for (int i = 0; i < 5; i++) {
            insert(event -> { });
        }

        assertThat(claimBatch(3)).hasSize(3);
        assertThat(claimBatch(3)).hasSize(2);
        assertThat(claimBatch(3)).isEmpty();
    }

    @Test
    void claimBatch_reclaimsStaleClaimsButNotFreshOnes() {
        Long stale = insert(event -> event.markProcessing(NOW.minus(CLAIM_TIMEOUT).minusSeconds(1), "crashed-worker"));
        Long fresh = insert(event -> event.markProcessing(NOW.minusSeconds(5), "live-worker"));

        List<ClaimedOutboxEvent<Long>> claimed = claimBatch(10);

        assertThat(claimed).extracting(ClaimedOutboxEvent::id).containsExactly(stale);
        assertThat(reload(stale).isClaimedBy("crashed-worker")).isFalse();
        assertThat(reload(stale).getAttemptCount()).isEqualTo(2);
        assertThat(reload(fresh).isClaimedBy("live-worker")).isTrue();
    }

    @Test
    void claimOne_claimsAReadyRowExactlyOnce() {
        Long ready = insert(event -> { });
        Long notYetAvailable = insert(event -> event.markFailed("boom", NOW.plusSeconds(30)));

        Optional<ClaimedOutboxEvent<Long>> first = tx.execute(status -> store.claimOne(ready, NOW));
        Optional<ClaimedOutboxEvent<Long>> second = tx.execute(status -> store.claimOne(ready, NOW));

        assertThat(first).isPresent();
        assertThat(reload(ready).isClaimedBy(first.orElseThrow().claimToken())).isTrue();
        assertThat(second).isEmpty();
        Optional<ClaimedOutboxEvent<Long>> notReady = tx.execute(status -> store.claimOne(notYetAvailable, NOW));
        Optional<ClaimedOutboxEvent<Long>> missing = tx.execute(status -> store.claimOne(-1L, NOW));
        assertThat(notReady).isEmpty();
        assertThat(missing).isEmpty();
    }

    @Test
    void outcomeLock_detectsAClaimTakenOverAfterTimeout() {
        Long id = insert(event -> { });
        ClaimedOutboxEvent<Long> slowWorker = claimBatch(10).get(0);
        Instant afterTimeout = NOW.plus(CLAIM_TIMEOUT).plusSeconds(1);
        List<ClaimedOutboxEvent<Long>> takeover = tx.execute(status -> store.claimBatch(
                new OutboxClaimRequest(RETRYABLE, afterTimeout, afterTimeout.minus(CLAIM_TIMEOUT), 10)));

        Boolean slowWorkerStillOwns = tx.execute(status -> store.findByIdForUpdate(id)
                .map(row -> row.isClaimedBy(slowWorker.claimToken()))
                .orElse(false));

        assertThat(takeover).extracting(ClaimedOutboxEvent::id).containsExactly(id);
        assertThat(slowWorkerStillOwns).isFalse();
        assertThat(reload(id).isClaimedBy(takeover.get(0).claimToken())).isTrue();
    }

    @Test
    void concurrentWorkers_neverClaimTheSameRowTwice() throws Exception {
        int rows = 200;
        int workers = 4;
        tx.executeWithoutResult(status -> {
            List<T> events = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                events.add(newEvent());
            }
            store.saveAll(events);
        });

        Set<Long> claimedIds = ConcurrentHashMap.newKeySet();
        AtomicInteger duplicates = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int w = 0; w < workers; w++) {
                results.add(pool.submit(() -> {
                    start.await();
                    int won = 0;
                    int emptyRounds = 0;
                    while (emptyRounds < 3) {
                        List<ClaimedOutboxEvent<Long>> batch = claimBatch(10);
                        if (batch.isEmpty()) {
                            emptyRounds++;
                            continue;
                        }
                        emptyRounds = 0;
                        for (ClaimedOutboxEvent<Long> claim : batch) {
                            if (!claimedIds.add(claim.id())) {
                                duplicates.incrementAndGet();
                            }
                        }
                        won += batch.size();
                    }
                    return won;
                }));
            }
            start.countDown();
            int total = 0;
            for (Future<Integer> result : results) {
                total += result.get(60, TimeUnit.SECONDS);
            }

            assertThat(duplicates).hasValue(0);
            assertThat(total).isEqualTo(rows);
            assertThat(claimedIds).hasSize(rows);
        } finally {
            pool.shutdownNow();
        }
    }

    private List<ClaimedOutboxEvent<Long>> claimBatch(int limit) {
        OutboxClaimRequest request = new OutboxClaimRequest(RETRYABLE, NOW, NOW.minus(CLAIM_TIMEOUT), limit);
        return tx.execute(status -> store.claimBatch(request));
    }

    private Long insert(java.util.function.Consumer<T> prepare) {
        return tx.execute(status -> {
            T event = newEvent();
            prepare.accept(event);
            store.saveAll(List.of(event));
            return event.getId();
        });
    }

    private T reload(Long id) {
        return tx.execute(status -> entityManager.find(entityType(), id));
    }

    protected T newEvent() {
        SerializedEvent serialized = new SerializedEvent("com.example.Happened", "{}", 1, "{}");
        return rowFactory().create(UUID.randomUUID(), "Product", "p-1", serialized, NOW.minusSeconds(120));
    }
}
