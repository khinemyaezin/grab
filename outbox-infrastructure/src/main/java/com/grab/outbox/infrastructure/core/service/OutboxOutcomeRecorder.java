package com.grab.outbox.infrastructure.core.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxRetryPolicy;
import com.grab.outbox.infrastructure.core.port.out.OutboxClaimStore;
import com.grab.outbox.infrastructure.core.port.out.OutboxObserver;
import com.grab.outbox.infrastructure.core.port.out.OutboxTransactions;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public final class OutboxOutcomeRecorder<T extends OutboxEntry<ID>, ID> {

    private static final Logger log = Loggers.getLogger(OutboxOutcomeRecorder.class);
    private static final int MAX_ERROR_LENGTH = 2000;

    private final String module;
    private final OutboxClaimStore<T, ID> outboxStore;
    private final OutboxTransactions transactions;
    private final OutboxRetryPolicy retryPolicy;
    private final OutboxObserver observer;
    private final Duration claimTimeout;
    private final Clock clock;

    public OutboxOutcomeRecorder(String module, OutboxClaimStore<T, ID> outboxStore,
                                 OutboxTransactions transactions, OutboxRetryPolicy retryPolicy,
                                 OutboxObserver observer, Duration claimTimeout, Clock clock) {
        this.module = module;
        this.outboxStore = outboxStore;
        this.transactions = transactions;
        this.retryPolicy = retryPolicy;
        this.observer = observer;
        this.claimTimeout = claimTimeout;
        this.clock = clock;
    }

    /** Runs inside the dispatch transaction so a lost fence can roll back listener work. */
    public boolean markPublished(ClaimedOutboxEvent<ID> claim) {
        Optional<T> stillClaimed = outboxStore.findByIdForUpdate(claim.id())
                .filter(event -> event.isClaimedBy(claim.claimToken()));
        if (stillClaimed.isEmpty()) {
            return false;
        }
        T event = stillClaimed.get();
        Instant now = clock.instant();
        event.markPublished(now);
        observer.published(module, event.getEventType(), lag(event, now));
        return true;
    }

    public void releaseUndispatched(Collection<ClaimedOutboxEvent<ID>> claims) {
        int released = 0;
        for (ClaimedOutboxEvent<ID> claim : claims) {
            if (releaseClaim(claim)) {
                released++;
            }
        }
        if (released > 0) {
            log.info("Released {} undispatched {} outbox claim(s) on shutdown", released, module);
            observer.claimsReleased(module, released);
        }
    }

    public void recordFailure(ClaimedOutboxEvent<ID> claimedEvent, RuntimeException exception, boolean poison) {
        String error = abbreviate(exception);
        try {
            transactions.inNewTransaction(() -> {
                Optional<T> claimedRow = outboxStore.findByIdForUpdate(claimedEvent.id())
                        .filter(event -> event.isClaimedBy(claimedEvent.claimToken()));
                if (claimedRow.isEmpty()) {
                    reportClaimLost(claimedEvent, "on failure");
                    return null;
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
                    return null;
                }

                Duration backoff = retryPolicy.backoff(attempt);
                event.markFailed(error, now.plus(backoff));
                observer.failed(module, event.getEventType(), attempt);
                log.warn("Failed to deliver {} outbox event id={} eventId={} type={} (attempt {}/{}), retrying in {}: {}",
                        module, event.getId(), event.getEventId(), event.getEventType(), attempt,
                        retryPolicy.maxAttempts(), backoff, error);
                return null;
            });
        } catch (RuntimeException outcomeFailure) {
            log.error("Could not record failure for {} outbox id={}; claim will expire after {}",
                    module, claimedEvent.id(), claimTimeout, outcomeFailure);
        }
    }

    public boolean releaseClaim(ClaimedOutboxEvent<ID> claimedEvent) {
        try {
            Boolean released = transactions.inNewTransaction(() -> outboxStore.findByIdForUpdate(claimedEvent.id())
                    .filter(event -> event.isClaimedBy(claimedEvent.claimToken()))
                    .map(event -> {
                        event.releaseClaim(clock.instant());
                        return true;
                    })
                    .orElse(false));
            return Boolean.TRUE.equals(released);
        } catch (RuntimeException exception) {
            log.warn("Could not release {} outbox claim id={}; it will expire after {}",
                    module, claimedEvent.id(), claimTimeout, exception);
            return false;
        }
    }

    public void reportClaimLost(ClaimedOutboxEvent<ID> claimedEvent, String phase) {
        observer.claimLost(module);
        log.warn("Lost claim on {} outbox id={} {}; another worker owns it now (claim timeout {} too short?)",
                module, claimedEvent.id(), phase, claimTimeout);
    }

    private static Duration lag(OutboxEntry<?> event, Instant now) {
        Instant occurredAt = event.getOccurredAt();
        return occurredAt == null ? Duration.ZERO : Duration.between(occurredAt, now);
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
