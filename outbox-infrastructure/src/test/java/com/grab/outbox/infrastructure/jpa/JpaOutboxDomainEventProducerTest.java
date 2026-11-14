package com.grab.outbox.infrastructure.jpa;

import com.grab.framework.domain.Event;
import com.grab.framework.logger.slf4j.TraceContext;
import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.outbox.OutboxWorkHandler;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.OutboxRowFactory;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.StubOutboxEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaOutboxDomainEventProducerTest {

    private OutboxStore<StubOutboxEvent, Long> outboxStore;
    private OutboxEventSerializer serializer;
    private OutboxRowFactory<StubOutboxEvent> rowFactory;
    private RecordingRelay relay;
    private JpaOutboxDomainEventProducer<StubOutboxEvent, Long> producer;

    @BeforeEach
    void setUp() {
        outboxStore = mock(OutboxStore.class);
        serializer = mock(OutboxEventSerializer.class);
        rowFactory = mock(OutboxRowFactory.class);
        relay = new RecordingRelay();
        producer = new JpaOutboxDomainEventProducer<>(outboxStore, serializer, rowFactory, relay);
    }

    @AfterEach
    void tearDown() {
        TraceContext.clear();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void produce_withoutMdc_leavesHeadersUnchanged() {
        Event event = new DummyEvent();
        SerializedEvent serialized = new SerializedEvent("type", "payload", 1, "{}");
        when(serializer.serialize(event)).thenReturn(serialized);
        when(rowFactory.create(any(UUID.class), eq("Product"), eq("p-1"), any(SerializedEvent.class), any(Instant.class)))
                .thenReturn(new StubOutboxEvent(1L, "{}"));

        producer.produce("Product", "p-1", List.of(event));

        verify(rowFactory).create(any(UUID.class), eq("Product"), eq("p-1"), any(SerializedEvent.class), any(Instant.class));
        verify(outboxStore).flush();
    }

    @Test
    void produce_withMdc_stampsTraceIdIntoHeaders() {
        Event event = new DummyEvent();
        SerializedEvent serialized = new SerializedEvent(
                "type",
                "payload",
                1,
                "{\"contentType\":\"application/json\"}"
        );
        when(serializer.serialize(event)).thenReturn(serialized);
        ArgumentCaptor<SerializedEvent> captor = ArgumentCaptor.forClass(SerializedEvent.class);
        when(rowFactory.create(any(UUID.class), eq("Product"), eq("p-1"), captor.capture(), any(Instant.class)))
                .thenReturn(new StubOutboxEvent(1L, "{}"));

        TraceContext.put("http-trace");
        producer.produce("Product", "p-1", List.of(event));

        SerializedEvent stamped = captor.getValue();
        assertTrue(stamped.headers().contains("\"contentType\":\"application/json\""));
        assertTrue(stamped.headers().contains("\"traceId\":\"http-trace\""));
        assertFalse(stamped.headers().equals("{}"));
        assertTrue(stamped.headers().contains("\"eventId\""));
        assertEquals("type", stamped.eventType());
    }

    @Test
    void produce_assignsDistinctEventIdPerRowAndStampsItIntoHeaders() {
        Event first = new DummyEvent();
        Event second = new DummyEvent();
        SerializedEvent serialized = new SerializedEvent("type", "payload", 1, "{}");
        when(serializer.serialize(any(Event.class))).thenReturn(serialized);
        ArgumentCaptor<UUID> ids = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<SerializedEvent> rows = ArgumentCaptor.forClass(SerializedEvent.class);
        when(rowFactory.create(ids.capture(), eq("Product"), eq("p-1"), rows.capture(), any(Instant.class)))
                .thenReturn(new StubOutboxEvent(1L, "{}"));

        producer.produce("Product", "p-1", List.of(first, second));

        assertEquals(2, ids.getAllValues().size());
        assertFalse(ids.getAllValues().get(0).equals(ids.getAllValues().get(1)));
        for (int i = 0; i < 2; i++) {
            assertEquals(ids.getAllValues().get(i),
                    com.grab.framework.outbox.OutboxEventHeaders.extractEventId(rows.getAllValues().get(i).headers()));
        }
    }

    @Test
    void produce_afterCommit_enqueuesHotIds() {
        Event event = new DummyEvent();
        SerializedEvent serialized = new SerializedEvent("type", "payload", 1, "{}");
        when(serializer.serialize(event)).thenReturn(serialized);
        when(rowFactory.create(any(UUID.class), eq("Product"), eq("p-1"), any(SerializedEvent.class), any(Instant.class)))
                .thenReturn(new StubOutboxEvent(7L, "{}", Instant.now().minusSeconds(1)));

        beginTransaction();
        producer.produce("Product", "p-1", List.of(event));
        assertTrue(relay.hotIds.isEmpty());

        triggerAfterCommit();

        assertEquals(List.of(7L), relay.hotIds);
    }

    @Test
    void produce_rollback_doesNotEnqueueHot() {
        Event event = new DummyEvent();
        SerializedEvent serialized = new SerializedEvent("type", "payload", 1, "{}");
        when(serializer.serialize(event)).thenReturn(serialized);
        when(rowFactory.create(any(UUID.class), eq("Product"), eq("p-1"), any(SerializedEvent.class), any(Instant.class)))
                .thenReturn(new StubOutboxEvent(1L, "{}"));

        beginTransaction();
        producer.produce("Product", "p-1", List.of(event));
        triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertTrue(relay.hotIds.isEmpty());
    }

    @Test
    void produce_skipsDelayedRowsFromHotQueue() {
        Event event = new DummyEvent();
        SerializedEvent serialized = new SerializedEvent("type", "payload", 1, "{}");
        when(serializer.serialize(event)).thenReturn(serialized);
        when(rowFactory.create(any(UUID.class), eq("Product"), eq("p-1"), any(SerializedEvent.class), any(Instant.class)))
                .thenReturn(new StubOutboxEvent(3L, "{}", Instant.now().plusSeconds(300)));

        beginTransaction();
        producer.produce("Product", "p-1", List.of(event));
        triggerAfterCommit();

        assertTrue(relay.hotIds.isEmpty());
    }

    @Test
    void produce_whenHotQueueDrops_doesNotThrow() {
        Event event = new DummyEvent();
        SerializedEvent serialized = new SerializedEvent("type", "payload", 1, "{}");
        when(serializer.serialize(event)).thenReturn(serialized);
        when(rowFactory.create(any(UUID.class), eq("Product"), eq("p-1"), any(SerializedEvent.class), any(Instant.class)))
                .thenReturn(new StubOutboxEvent(1L, "{}"));
        relay.acceptHot = false;

        beginTransaction();
        producer.produce("Product", "p-1", List.of(event));
        triggerAfterCommit();

        assertTrue(relay.hotIds.isEmpty());
    }

    private static void beginTransaction() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    private static void triggerAfterCommit() {
        List<TransactionSynchronization> synchronizations =
                new ArrayList<>(TransactionSynchronizationManager.getSynchronizations());
        synchronizations.forEach(TransactionSynchronization::afterCommit);
        synchronizations.forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    private static void triggerAfterCompletion(int status) {
        List<TransactionSynchronization> synchronizations =
                new ArrayList<>(TransactionSynchronizationManager.getSynchronizations());
        synchronizations.forEach(sync -> sync.afterCompletion(status));
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    private record DummyEvent() implements Event {
    }

    static final class RecordingRelay implements OutboxRelay<Long> {
        final List<Long> hotIds = new ArrayList<>();
        boolean acceptHot = true;

        @Override
        public boolean enqueueHot(Long id) {
            if (!acceptHot) {
                return false;
            }
            hotIds.add(id);
            return true;
        }

        @Override
        public boolean enqueueCold(ClaimedOutboxEvent<Long> claimed) {
            return false;
        }

        @Override
        public int coldQueueRemainingCapacity() {
            return 0;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void setHandler(OutboxWorkHandler<Long> handler) {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
    }
}
