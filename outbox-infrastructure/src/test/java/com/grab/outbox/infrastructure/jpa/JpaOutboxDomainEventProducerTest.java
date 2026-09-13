package com.grab.outbox.infrastructure.jpa;

import com.grab.framework.domain.Event;
import com.grab.framework.logger.slf4j.TraceContext;
import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.OutboxWorkHandler;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.OutboxRowFactory;
import com.grab.outbox.infrastructure.OutboxStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
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
        when(rowFactory.create(eq("Product"), eq("p-1"), eq(serialized), any(LocalDateTime.class)))
                .thenReturn(new StubOutboxEvent());

        producer.produce("Product", "p-1", List.of(event));

        verify(rowFactory).create(eq("Product"), eq("p-1"), eq(serialized), any(LocalDateTime.class));
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
        when(rowFactory.create(eq("Product"), eq("p-1"), captor.capture(), any(LocalDateTime.class)))
                .thenReturn(new StubOutboxEvent());

        TraceContext.put("http-trace");
        producer.produce("Product", "p-1", List.of(event));

        SerializedEvent stamped = captor.getValue();
        assertTrue(stamped.headers().contains("\"contentType\":\"application/json\""));
        assertTrue(stamped.headers().contains("\"traceId\":\"http-trace\""));
        assertFalse(stamped.headers().equals("{}"));
        assertEquals("type", stamped.eventType());
    }

    @Test
    void produce_afterCommit_enqueuesHotIds() {
        Event event = new DummyEvent();
        SerializedEvent serialized = new SerializedEvent("type", "payload", 1, "{}");
        when(serializer.serialize(event)).thenReturn(serialized);
        when(rowFactory.create(eq("Product"), eq("p-1"), eq(serialized), any(LocalDateTime.class)))
                .thenReturn(new StubOutboxEvent(7L, LocalDateTime.now().minusSeconds(1)));

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
        when(rowFactory.create(eq("Product"), eq("p-1"), eq(serialized), any(LocalDateTime.class)))
                .thenReturn(new StubOutboxEvent());

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
        when(rowFactory.create(eq("Product"), eq("p-1"), eq(serialized), any(LocalDateTime.class)))
                .thenReturn(new StubOutboxEvent(3L, LocalDateTime.now().plusMinutes(5)));

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
        when(rowFactory.create(eq("Product"), eq("p-1"), eq(serialized), any(LocalDateTime.class)))
                .thenReturn(new StubOutboxEvent());
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

    static final class StubOutboxEvent implements OutboxEntry<Long> {
        private OutboxStatus status = OutboxStatus.NEW;
        private String claimToken;
        private final Long id;
        private final LocalDateTime availableAt;

        StubOutboxEvent() {
            this(1L, LocalDateTime.now().minusSeconds(1));
        }

        StubOutboxEvent(Long id, LocalDateTime availableAt) {
            this.id = id;
            this.availableAt = availableAt;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getEventType() {
            return "type";
        }

        @Override
        public String getPayload() {
            return "payload";
        }

        @Override
        public int getEventVersion() {
            return 1;
        }

        @Override
        public String getHeaders() {
            return "{}";
        }

        @Override
        public OutboxStatus getStatus() {
            return status;
        }

        @Override
        public String getClaimToken() {
            return claimToken;
        }

        @Override
        public LocalDateTime getAvailableAt() {
            return availableAt;
        }

        @Override
        public void markProcessing(LocalDateTime now, String claimToken) {
            this.status = OutboxStatus.PROCESSING;
            this.claimToken = claimToken;
        }

        @Override
        public void markPublished(LocalDateTime now) {
            this.status = OutboxStatus.PUBLISHED;
        }

        @Override
        public void markFailed(LocalDateTime now, String error, Duration retryDelay) {
            this.status = OutboxStatus.FAILED;
        }
    }
}
