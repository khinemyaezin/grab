package com.grab.outbox.infrastructure.adapter.out.jpa;

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
import com.grab.outbox.infrastructure.adapter.out.spring.SpringOutboxCommitHook;
import com.grab.outbox.infrastructure.core.port.out.OutboxWriter;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JpaOutboxDomainEventProducer<T extends OutboxEntry<ID>, ID> implements DomainEventProducer {

    private static final Logger log = Loggers.getLogger(JpaOutboxDomainEventProducer.class);

    private final String module;
    private final OutboxWriter<T> outboxStore;
    private final OutboxEventSerializer serializer;
    private final OutboxRowFactory<T> rowFactory;
    private final OutboxRelay<ID> relay;
    private final Clock clock;

    public JpaOutboxDomainEventProducer(
            String module,
            OutboxWriter<T> outboxStore,
            OutboxEventSerializer serializer,
            OutboxRowFactory<T> rowFactory
    ) {
        this(module, outboxStore, serializer, rowFactory, OutboxRelay.noop());
    }

    public JpaOutboxDomainEventProducer(
            String module,
            OutboxWriter<T> outboxStore,
            OutboxEventSerializer serializer,
            OutboxRowFactory<T> rowFactory,
            OutboxRelay<ID> relay
    ) {
        this(module, outboxStore, serializer, rowFactory, relay, Clock.systemUTC());
    }

    public JpaOutboxDomainEventProducer(
            String module,
            OutboxWriter<T> outboxStore,
            OutboxEventSerializer serializer,
            OutboxRowFactory<T> rowFactory,
            OutboxRelay<ID> relay,
            Clock clock
    ) {
        this.module = module;
        this.outboxStore = outboxStore;
        this.serializer = serializer;
        this.rowFactory = rowFactory;
        this.relay = relay == null ? OutboxRelay.noop() : relay;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @Override
    public void produce(String aggregateType, String aggregateId, List<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        log.info("Producing {} {} outbox event(s) for aggregateType={}, aggregateId={}",
                events.size(), module, aggregateType, aggregateId);
        Instant now = clock.instant();
        List<T> outboxEvents = events.stream()
                .map(event -> toRow(aggregateType, aggregateId, event, now))
                .toList();
        outboxStore.saveAll(outboxEvents);
        outboxStore.flush();
        enqueueHotAfterCommit(outboxEvents, now);
    }

    private T toRow(String aggregateType, String aggregateId, Event event, Instant now) {
        UUID eventId = UUID.randomUUID();
        SerializedEvent serialized = stampHeaders(serializer.serialize(event), eventId);
        return rowFactory.create(eventId, aggregateType, aggregateId, serialized, now);
    }

    private void enqueueHotAfterCommit(List<T> outboxEvents, Instant now) {
        if (!relay.isActive()) {
            return;
        }
        List<ID> hotIds = new ArrayList<>();
        for (T event : outboxEvents) {
            if (event.getId() == null) {
                continue;
            }
            Instant availableAt = event.getAvailableAt();
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

    private static SerializedEvent stampHeaders(SerializedEvent serialized, UUID eventId) {
        String headers = OutboxEventHeaders.mergeEventId(serialized.headers(), eventId);
        String traceId = TraceContext.current();
        if (traceId != null && !traceId.isBlank()) {
            headers = OutboxEventHeaders.mergeTraceId(headers, traceId);
        }
        return new SerializedEvent(
                serialized.eventType(),
                serialized.payload(),
                serialized.eventVersion(),
                headers
        );
    }
}
