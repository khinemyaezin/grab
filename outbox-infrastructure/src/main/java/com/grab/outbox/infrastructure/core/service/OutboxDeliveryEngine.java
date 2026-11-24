package com.grab.outbox.infrastructure.core.service;

import com.grab.framework.domain.Event;
import com.grab.framework.logger.slf4j.LogContext;
import com.grab.framework.logger.slf4j.TraceContext;
import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxDeliveryContext;
import com.grab.framework.outbox.OutboxDispatchMetadata;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventHeaders;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxPoisonEventException;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.core.port.in.DeliverOutboxEventsUseCase;
import com.grab.outbox.infrastructure.core.port.out.OutboxClaimRequest;
import com.grab.outbox.infrastructure.core.port.out.OutboxClaimStore;
import com.grab.outbox.infrastructure.core.port.out.OutboxTransactions;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class OutboxDeliveryEngine<T extends OutboxEntry<ID>, ID> implements DeliverOutboxEventsUseCase<ID> {

    private static final List<OutboxStatus> RETRYABLE_STATUSES = List.of(OutboxStatus.NEW, OutboxStatus.FAILED);

    private final OutboxClaimStore<T, ID> outboxStore;
    private final OutboxEventSerializer serializer;
    private final OutboxEventDispatcher dispatcher;
    private final OutboxTransactions transactions;
    private final OutboxRelay<ID> relay;
    private final OutboxOutcomeRecorder<T, ID> outcomes;
    private final Runnable cleanup;
    private final int batchSize;
    private final Duration claimTimeout;
    private final Clock clock;

    public OutboxDeliveryEngine(OutboxClaimStore<T, ID> outboxStore, OutboxEventSerializer serializer,
                                OutboxEventDispatcher dispatcher, OutboxTransactions transactions, OutboxRelay<ID> relay,
                                OutboxOutcomeRecorder<T, ID> outcomes, Runnable cleanup, int batchSize,
                                Duration claimTimeout, Clock clock) {
        this.outboxStore = outboxStore;
        this.serializer = serializer;
        this.dispatcher = dispatcher;
        this.transactions = transactions;
        this.relay = relay;
        this.outcomes = outcomes;
        this.cleanup = cleanup;
        this.batchSize = batchSize;
        this.claimTimeout = claimTimeout;
        this.clock = clock;
    }

    @Override
    public void pollOnce() {
        if (!relay.isActive()) {
            List<ClaimedOutboxEvent<ID>> claimedEvents = claimBatch(batchSize);
            for (ClaimedOutboxEvent<ID> claimedEvent : claimedEvents) {
                deliverClaimed(claimedEvent);
            }
            return;
        }
        int capacity = relay.coldQueueRemainingCapacity();
        if (capacity <= 0) {
            return;
        }
        int limit = Math.min(batchSize, capacity);
        List<ClaimedOutboxEvent<ID>> claimedEvents = claimBatch(limit);
        for (int index = 0; index < claimedEvents.size(); index++) {
            ClaimedOutboxEvent<ID> claimedEvent = claimedEvents.get(index);
            if (!relay.enqueueCold(claimedEvent)) {
                for (ClaimedOutboxEvent<ID> unqueued : claimedEvents.subList(index, claimedEvents.size())) {
                    outcomes.releaseClaim(unqueued);
                }
                break;
            }
        }
    }

    @Override
    public void cleanupPublished() {
        cleanup.run();
    }

    @Override
    public void deliverHot(ID id) {
        Optional<ClaimedOutboxEvent<ID>> claimed = claimById(id);
        claimed.ifPresent(this::deliverClaimed);
    }

    @Override
    public void deliverClaimed(ClaimedOutboxEvent<ID> claim) {
        Optional<T> row = transactions.readOnly(() ->
                outboxStore.findById(claim.id())
                        .filter(event -> event.isClaimedBy(claim.claimToken()))
        );
        if (row.isEmpty()) {
            outcomes.reportClaimLost(claim, "before dispatch");
            return;
        }
        T event = row.get();
        String headerTraceId = OutboxEventHeaders.extractTraceId(event.getHeaders());
        String traceId = headerTraceId == null || headerTraceId.isBlank() ? TraceContext.generate() : headerTraceId;
        UUID eventId = event.getEventId();
        String eventIdValue = eventId == null ? null : eventId.toString();

        TraceContext.run(traceId, () -> LogContext.run(LogContext.EVENT_ID, eventIdValue, () -> {
            try {
                dispatch(event);
                boolean published = transactions.inNewTransaction(() -> outcomes.markPublished(claim));
                if (!published) {
                    outcomes.reportClaimLost(claim, "after dispatch");
                }
            } catch (OutboxPoisonEventException exception) {
                outcomes.recordFailure(claim, exception, true);
            } catch (RuntimeException exception) {
                outcomes.recordFailure(claim, exception, false);
            }
        }));
    }

    public void shutdown() {
        relay.stop();
        List<ClaimedOutboxEvent<ID>> undrained = relay.drainCold();
        outcomes.releaseUndispatched(undrained);
    }

    private List<ClaimedOutboxEvent<ID>> claimBatch(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        List<ClaimedOutboxEvent<ID>> claimedEvents = transactions.inNewTransaction(() -> {
            Instant now = clock.instant();
            Instant staleBefore = now.minus(claimTimeout);
            OutboxClaimRequest request = new OutboxClaimRequest(RETRYABLE_STATUSES, now, staleBefore, limit);
            return outboxStore.claimBatch(request);
        });

        return claimedEvents == null ? List.of() : claimedEvents;
    }

    private Optional<ClaimedOutboxEvent<ID>> claimById(ID id) {
        return transactions.inNewTransaction(() -> {
            Instant now = clock.instant();
            return outboxStore.claimOne(id, now);
        });
    }

    private void dispatch(T event) {
        String eventType = event.getEventType();
        String eventPayload = event.getPayload();
        int eventVersion = event.getEventVersion();
        String eventHeaders = event.getHeaders();
        SerializedEvent serializedEvent = new SerializedEvent(
                eventType,
                eventPayload,
                eventVersion,
                eventHeaders
        );
        Event payload = serializer.deserialize(serializedEvent);
        UUID metadataEventId = event.getEventId();
        String aggregateType = event.getAggregateType();
        String aggregateId = event.getAggregateId();
        Instant occurredAt = event.getOccurredAt();
        OutboxDispatchMetadata metadata = new OutboxDispatchMetadata(
                metadataEventId,
                eventType,
                aggregateType,
                aggregateId,
                eventVersion,
                occurredAt,
                eventPayload,
                eventHeaders
        );
        OutboxDeliveryContext.run(metadata, () -> dispatcher.dispatch(payload));
    }
}
