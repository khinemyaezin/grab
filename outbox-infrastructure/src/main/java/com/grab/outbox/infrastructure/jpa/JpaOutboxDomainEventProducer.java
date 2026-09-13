package com.grab.outbox.infrastructure.jpa;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.slf4j.TraceContext;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxEventHeaders;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.OutboxRowFactory;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.SpringOutboxCommitHook;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JpaOutboxDomainEventProducer<T extends OutboxEntry<ID>, ID> implements DomainEventProducer {

    private static final Logger log = Loggers.getLogger(JpaOutboxDomainEventProducer.class);

    private final OutboxStore<T, ID> outboxStore;
    private final OutboxEventSerializer serializer;
    private final OutboxRowFactory<T> rowFactory;
    private final OutboxRelay<ID> relay;

    public JpaOutboxDomainEventProducer(
            OutboxStore<T, ID> outboxStore,
            OutboxEventSerializer serializer,
            OutboxRowFactory<T> rowFactory
    ) {
        this(outboxStore, serializer, rowFactory, OutboxRelay.noop());
    }

    public JpaOutboxDomainEventProducer(
            OutboxStore<T, ID> outboxStore,
            OutboxEventSerializer serializer,
            OutboxRowFactory<T> rowFactory,
            OutboxRelay<ID> relay
    ) {
        this.outboxStore = outboxStore;
        this.serializer = serializer;
        this.rowFactory = rowFactory;
        this.relay = relay == null ? OutboxRelay.noop() : relay;
    }

    @Override
    public void produce(String aggregateType, String aggregateId, List<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<T> outboxEvents = events.stream()
                .map(event -> rowFactory.create(aggregateType, aggregateId, stampTraceId(serializer.serialize(event)), now))
                .toList();
        outboxStore.saveAll(outboxEvents);
        outboxStore.flush();
        enqueueHotAfterCommit(outboxEvents, now);
    }

    private void enqueueHotAfterCommit(List<T> outboxEvents, LocalDateTime now) {
        if (!relay.isActive()) {
            return;
        }
        List<ID> hotIds = new ArrayList<>();
        for (T event : outboxEvents) {
            if (event.getId() == null) {
                continue;
            }
            LocalDateTime availableAt = event.getAvailableAt();
            if (availableAt != null && availableAt.isAfter(now)) {
                continue;
            }
            hotIds.add(event.getId());
        }
        if (hotIds.isEmpty()) {
            return;
        }
        SpringOutboxCommitHook.afterCommit(() -> {
            for (ID id : hotIds) {
                try {
                    relay.enqueueHot(id);
                } catch (RuntimeException exception) {
                    log.warn("Hot enqueue failed for outbox id={}; poller will recover", id, exception);
                }
            }
        });
    }

    private static SerializedEvent stampTraceId(SerializedEvent serialized) {
        String traceId = TraceContext.current();
        if (traceId == null || traceId.isBlank()) {
            return serialized;
        }
        return new SerializedEvent(
                serialized.eventType(),
                serialized.payload(),
                serialized.eventVersion(),
                OutboxEventHeaders.mergeTraceId(serialized.headers(), traceId)
        );
    }
}
