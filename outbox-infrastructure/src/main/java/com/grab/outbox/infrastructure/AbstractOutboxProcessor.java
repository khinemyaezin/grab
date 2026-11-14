package com.grab.outbox.infrastructure;

import com.grab.framework.domain.Event;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.slf4j.LogContext;
import com.grab.framework.logger.slf4j.TraceContext;
import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxDeliveryContext;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventHeaders;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxPoisonEventException;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.outbox.OutboxRetryPolicy;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.OutboxWorkSource;
import com.grab.framework.outbox.QueuedOutboxWork;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * At-least-once outbox delivery for one module.
 *
 * <ul>
 *   <li>Claims go through the store's configured {@code OutboxClaimStrategy} (e.g. {@code SKIP LOCKED} or a
 *       conditional update), so replicas compete for rows instead of serialising on them.</li>
 *   <li>Every status transition after dispatch re-reads the row under a lock and only applies if this worker
 *       still owns the claim token; a claim lost to another replica is discarded and reported.</li>
 *   <li>Failures back off exponentially with jitter. After {@code maxAttempts}, or immediately for poison
 *       payloads, the row moves to {@link OutboxStatus#DEAD}.</li>
 *   <li>On shutdown, claimed but undispatched cold work is released back to {@code FAILED/availableAt=now}.</li>
 * </ul>
 */
public abstract class AbstractOutboxProcessor<T extends OutboxEntry<ID>, ID> implements DisposableBean {

    private static final Logger log = Loggers.getLogger(AbstractOutboxProcessor.class);
    private static final int MAX_ERROR_LENGTH = 2000;
    private static final List<OutboxStatus> RETRYABLE_STATUSES = List.of(OutboxStatus.NEW, OutboxStatus.FAILED);
    private static final List<OutboxStatus> PENDING_STATUSES =
            List.of(OutboxStatus.NEW, OutboxStatus.FAILED, OutboxStatus.PROCESSING);

    private final String module;
    private final OutboxStore<T, ID> outboxStore;
    private final OutboxEventSerializer serializer;
    private final OutboxEventDispatcher dispatcher;
    private final TransactionTemplate claimTransactionTemplate;
    private final TransactionTemplate publishTransactionTemplate;
    private final TransactionTemplate outcomeTransactionTemplate;
    private final TransactionTemplate cleanupTransactionTemplate;
    private final TransactionTemplate readTransactionTemplate;
    private final OutboxProcessorSettings settings;
    private final OutboxRetryPolicy retryPolicy;
    private final OutboxRelay<ID> relay;
    private final OutboxObserver observer;
    private final SingletonJobExecutor singletonJobExecutor;
    private final SingletonJob cleanupJob;
    private final Clock clock;

    protected AbstractOutboxProcessor(
            String module,
            OutboxStore<T, ID> outboxStore,
            OutboxEventSerializer serializer,
            OutboxEventDispatcher dispatcher,
            PlatformTransactionManager transactionManager,
            OutboxProcessorSettings settings,
            OutboxRelay<ID> relay,
            OutboxObserver observer,
            SingletonJobExecutor singletonJobExecutor
    ) {
        this(module, outboxStore, serializer, dispatcher, transactionManager, settings, relay, observer,
                singletonJobExecutor, settings == null ? null : settings.retryPolicy(), Clock.systemUTC());
    }

    protected AbstractOutboxProcessor(
            String module,
            OutboxStore<T, ID> outboxStore,
            OutboxEventSerializer serializer,
            OutboxEventDispatcher dispatcher,
            PlatformTransactionManager transactionManager,
            OutboxProcessorSettings settings,
            OutboxRelay<ID> relay,
            OutboxObserver observer,
            SingletonJobExecutor singletonJobExecutor,
            OutboxRetryPolicy retryPolicy,
            Clock clock
    ) {
        this.module = Objects.requireNonNull(module, "module");
        this.outboxStore = Objects.requireNonNull(outboxStore, "outboxStore");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.settings = settings == null ? OutboxProcessorSettings.defaults() : settings;
        this.retryPolicy = retryPolicy == null ? this.settings.retryPolicy() : retryPolicy;
        this.relay = relay == null ? OutboxRelay.noop() : relay;
        this.observer = observer == null ? OutboxObserver.noop() : observer;
        this.singletonJobExecutor = singletonJobExecutor == null ? SingletonJobExecutor.local() : singletonJobExecutor;
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.cleanupJob = new SingletonJob(
                module + ".outbox.cleanup",
                this.settings.cleanupLockAtMostFor(),
                this.settings.cleanupLockAtLeastFor()
        );
        this.claimTransactionTemplate = new TransactionTemplate(transactionManager);
        this.publishTransactionTemplate = requiresNew(transactionManager);
        this.outcomeTransactionTemplate = requiresNew(transactionManager);
        this.cleanupTransactionTemplate = requiresNew(transactionManager);
        this.readTransactionTemplate = requiresNew(transactionManager);
        this.readTransactionTemplate.setReadOnly(true);
        if (this.relay.isActive()) {
            this.relay.setHandler(this::handleQueuedWork);
            this.relay.start();
        }
    }

    @Override
    public void destroy() {
        relay.stop();
        List<ClaimedOutboxEvent<ID>> undrained = relay.drainCold();
        int released = 0;
        for (ClaimedOutboxEvent<ID> claimed : undrained) {
            if (releaseClaim(claimed)) {
                released++;
            }
        }
        if (released > 0) {
            log.info("Released {} undispatched {} outbox claim(s) on shutdown", released, module);
            observer.claimsReleased(module, released);
        }
    }

    protected void processAvailableEvents() {
        if (!relay.isActive()) {
            List<ClaimedOutboxEvent<ID>> claimedEvents = claimBatch(settings.batchSize());
            for (ClaimedOutboxEvent<ID> claimedEvent : claimedEvents) {
                publishEvent(claimedEvent);
            }
            return;
        }
        int capacity = relay.coldQueueRemainingCapacity();
        if (capacity <= 0) {
            return;
        }
        int limit = Math.min(settings.batchSize(), capacity);
        List<ClaimedOutboxEvent<ID>> claimedEvents = claimBatch(limit);
        for (int index = 0; index < claimedEvents.size(); index++) {
            ClaimedOutboxEvent<ID> claimedEvent = claimedEvents.get(index);
            if (!relay.enqueueCold(claimedEvent)) {
                releaseClaims(claimedEvents.subList(index, claimedEvents.size()));
                break;
            }
        }
    }

    /**
     * Deletes published rows past retention in bounded batches. Runs on one replica at a time.
     */
    protected void cleanupPublishedEvents() {
        boolean ran = singletonJobExecutor.execute(cleanupJob, this::deletePublishedInBatches);
        if (!ran) {
            log.debug("Skipped {} outbox cleanup; another replica holds the lock", module);
        }
    }

    public String module() {
        return module;
    }

    /**
     * Undelivered rows for the {@code outbox.pending} / {@code outbox.oldest_pending_age_seconds} gauges.
     */
    public OutboxBacklog backlog() {
        OutboxBacklog backlog = readTransactionTemplate.execute(status -> outboxStore.backlog(PENDING_STATUSES));
        return backlog == null ? OutboxBacklog.EMPTY : backlog;
    }

    public long hotQueueDropped() {
        return relay.hotQueueDropped();
    }

    /**
     * Operator replay: moves up to {@code limit} {@code DEAD} rows with {@code fromId <= id <= toId} back to
     * {@code NEW}. The poller then delivers them like fresh events (consumers dedupe through their inbox).
     *
     * @return the number of requeued rows
     */
    public int replayDead(ID fromId, ID toId, int limit) {
        Objects.requireNonNull(fromId, "fromId");
        Objects.requireNonNull(toId, "toId");
        Integer replayed = cleanupTransactionTemplate.execute(status -> {
            List<T> dead = outboxStore.findDeadForReplay(fromId, toId, limit);
            Instant now = clock.instant();
            for (T event : dead) {
                event.requeue(now);
            }
            return dead.size();
        });
        int count = replayed == null ? 0 : replayed;
        log.warn("Operator replay requeued {} dead {} outbox row(s) in id range [{}, {}]", count, module, fromId, toId);
        return count;
    }

    private void deletePublishedInBatches() {
        Instant cutoff = clock.instant().minus(settings.retention());
        int batch = settings.cleanupBatchSize();
        int total = 0;
        while (true) {
            Integer deleted = cleanupTransactionTemplate.execute(status ->
                    outboxStore.deletePublishedOlderThan(cutoff, batch));
            int count = deleted == null ? 0 : deleted;
            total += count;
            if (count < batch) {
                break;
            }
        }
        if (total > 0) {
            log.info("Deleted {} published {} outbox row(s) older than {}", total, module, cutoff);
            observer.cleanedUp(module, total);
        }
    }

    private void handleQueuedWork(QueuedOutboxWork<ID> work) {
        if (work.source() == OutboxWorkSource.COLD) {
            publishEvent(new ClaimedOutboxEvent<>(work.id(), work.claimToken()));
            return;
        }
        claimById(work.id()).ifPresent(this::publishEvent);
    }

    private List<ClaimedOutboxEvent<ID>> claimBatch(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        List<ClaimedOutboxEvent<ID>> claimedEvents = claimTransactionTemplate.execute(status -> {
            Instant now = clock.instant();
            Instant staleBefore = now.minus(settings.claimTimeout());
            OutboxClaimRequest request = new OutboxClaimRequest(RETRYABLE_STATUSES, now, staleBefore, limit);
            return outboxStore.claimBatch(request);
        });

        return claimedEvents == null ? List.of() : claimedEvents;
    }

    private Optional<ClaimedOutboxEvent<ID>> claimById(ID id) {
        ClaimedOutboxEvent<ID> claimed = claimTransactionTemplate.execute(status -> {
            Instant now = clock.instant();
            return outboxStore.claimOne(id, now).orElse(null);
        });
        return Optional.ofNullable(claimed);
    }

    private void publishEvent(ClaimedOutboxEvent<ID> claimedEvent) {
        try {
            publishTransactionTemplate.executeWithoutResult(status -> {
                Optional<T> claimedRow = outboxStore.findById(claimedEvent.id())
                        .filter(event -> event.isClaimedBy(claimedEvent.claimToken()));
                if (claimedRow.isEmpty()) {
                    reportClaimLost(claimedEvent, "before dispatch");
                    return;
                }

                dispatch(claimedRow.get());

                Optional<T> stillClaimed = outboxStore.findByIdForUpdate(claimedEvent.id())
                        .filter(event -> event.isClaimedBy(claimedEvent.claimToken()));
                if (stillClaimed.isEmpty()) {
                    status.setRollbackOnly();
                    reportClaimLost(claimedEvent, "after dispatch");
                    return;
                }

                T event = stillClaimed.get();
                Instant now = clock.instant();
                event.markPublished(now);
                observer.published(module, event.getEventType(), lag(event, now));
            });
        } catch (OutboxPoisonEventException exception) {
            recordFailure(claimedEvent, exception, true);
        } catch (RuntimeException exception) {
            recordFailure(claimedEvent, exception, false);
        }
    }

    private void dispatch(T event) {
        Event payload = serializer.deserialize(new SerializedEvent(
                event.getEventType(),
                event.getPayload(),
                event.getEventVersion(),
                event.getHeaders()
        ));
        String headerTraceId = OutboxEventHeaders.extractTraceId(event.getHeaders());
        String traceId = headerTraceId == null || headerTraceId.isBlank() ? TraceContext.generate() : headerTraceId;
        UUID eventId = event.getEventId();
        String eventIdValue = eventId == null ? null : eventId.toString();
        Runnable delivery = () -> OutboxDeliveryContext.run(eventId, () -> dispatcher.dispatch(payload));
        TraceContext.run(traceId, () -> LogContext.run(LogContext.EVENT_ID, eventIdValue, delivery));
    }

    private void recordFailure(ClaimedOutboxEvent<ID> claimedEvent, RuntimeException exception, boolean poison) {
        String error = abbreviate(exception);
        try {
            outcomeTransactionTemplate.executeWithoutResult(status -> {
                Optional<T> claimedRow = outboxStore.findByIdForUpdate(claimedEvent.id())
                        .filter(event -> event.isClaimedBy(claimedEvent.claimToken()));
                if (claimedRow.isEmpty()) {
                    reportClaimLost(claimedEvent, "on failure");
                    return;
                }

                T event = claimedRow.get();
                Instant now = clock.instant();
                int attempt = event.getAttemptCount();
                if (poison || retryPolicy.isExhausted(attempt)) {
                    event.markDead(now, error);
                    observer.dead(module, event.getEventType());
                    log.error("Dead-lettered {} outbox event id={} eventId={} type={} after {} attempt(s){}: {}",
                            module, event.getId(), event.getEventId(), event.getEventType(), attempt,
                            poison ? " (poison payload)" : "", error);
                    return;
                }

                Duration backoff = retryPolicy.backoff(attempt);
                event.markFailed(error, now.plus(backoff));
                observer.failed(module, event.getEventType(), attempt);
                log.warn("Failed to deliver {} outbox event id={} eventId={} type={} (attempt {}/{}), retrying in {}: {}",
                        module, event.getId(), event.getEventId(), event.getEventType(), attempt,
                        retryPolicy.maxAttempts(), backoff, error);
            });
        } catch (RuntimeException outcomeFailure) {
            log.error("Could not record failure for {} outbox id={}; claim will expire after {}",
                    module, claimedEvent.id(), settings.claimTimeout(), outcomeFailure);
        }
    }

    private void releaseClaims(List<ClaimedOutboxEvent<ID>> claims) {
        for (ClaimedOutboxEvent<ID> claim : claims) {
            releaseClaim(claim);
        }
    }

    private boolean releaseClaim(ClaimedOutboxEvent<ID> claimedEvent) {
        try {
            Boolean released = outcomeTransactionTemplate.execute(status -> outboxStore.findByIdForUpdate(claimedEvent.id())
                    .filter(event -> event.isClaimedBy(claimedEvent.claimToken()))
                    .map(event -> {
                        event.releaseClaim(clock.instant());
                        return true;
                    })
                    .orElse(false));
            return Boolean.TRUE.equals(released);
        } catch (RuntimeException exception) {
            log.warn("Could not release {} outbox claim id={}; it will expire after {}",
                    module, claimedEvent.id(), settings.claimTimeout(), exception);
            return false;
        }
    }

    private void reportClaimLost(ClaimedOutboxEvent<ID> claimedEvent, String phase) {
        observer.claimLost(module);
        log.warn("Lost claim on {} outbox id={} {}; another worker owns it now (claim timeout {} too short?)",
                module, claimedEvent.id(), phase, settings.claimTimeout());
    }

    private static Duration lag(OutboxEntry<?> event, Instant now) {
        Instant occurredAt = event.getOccurredAt();
        return occurredAt == null ? Duration.ZERO : Duration.between(occurredAt, now);
    }

    private static TransactionTemplate requiresNew(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }

    private static String abbreviate(RuntimeException exception) {
        String message = exception.getMessage();
        String value = message == null || message.isBlank() ? exception.getClass().getName() : message;
        if (value.length() <= MAX_ERROR_LENGTH) {
            return value;
        }

        return value.substring(0, MAX_ERROR_LENGTH);
    }
}
