package com.inventory.adapter.persistence.outbox;

import static org.mockito.ArgumentMatchers.argThat;
import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.adapter.out.spring.SpringOutboxTransactions;
import com.grab.outbox.infrastructure.core.port.out.OutboxObserver;
import com.grab.outbox.infrastructure.core.port.out.OutboxTransactions;
import com.grab.outbox.infrastructure.core.service.OutboxDeliveryEngine;
import com.grab.outbox.infrastructure.core.service.OutboxOutcomeRecorder;
import com.grab.outbox.infrastructure.core.service.OutboxRetentionCleaner;
import com.grab.outbox.infrastructure.module.OutboxProcessorSettings;
import com.inventory.domain.event.StockReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InventoryOutboxDeliveryTest {

    private JpaOutboxStore<InventoryOutboxEvent, Long> outboxStore;
    private OutboxEventSerializer serializer;
    private OutboxEventDispatcher dispatcher;
    private OutboxDeliveryEngine<InventoryOutboxEvent, Long> delivery;

    @BeforeEach
    void setUp() {
        outboxStore = mock(JpaOutboxStore.class);
        serializer = mock(OutboxEventSerializer.class);
        dispatcher = mock(OutboxEventDispatcher.class);
        Clock clock = Clock.systemUTC();
        OutboxProcessorSettings settings = OutboxProcessorSettings.defaults();
        OutboxObserver observer = OutboxObserver.NOOP;
        OutboxTransactions transactions = new SpringOutboxTransactions(new NoOpTransactionManager());
        OutboxOutcomeRecorder<InventoryOutboxEvent, Long> recorder = new OutboxOutcomeRecorder<>(
                "inventory", outboxStore, transactions, settings.retryPolicy(), observer, settings.claimTimeout(), clock);
        OutboxRetentionCleaner cleaner = new OutboxRetentionCleaner("inventory", outboxStore, transactions,
                observer, settings.retention(), settings.cleanupBatchSize(), clock);
        delivery = new OutboxDeliveryEngine<>(outboxStore, serializer, dispatcher, transactions, OutboxRelay.noop(),
                recorder, cleaner::cleanupPublished, 10, settings.claimTimeout(), clock);
    }

    @Test
    void processAvailableEvents_dispatchesAndMarksPublished() {
        Event payload = new StockReceivedEvent(id("inventory-1"), "SKU-1", 10, id("location-1"), LocalDateTime.now());
        InventoryOutboxEvent outboxEvent = pendingEvent(1L, "event-type", "payload");

        when(outboxStore.claimBatch(argThat(request -> request != null
                && request.limit() == 10
                && request.retryableStatuses().equals(List.of(OutboxStatus.NEW, OutboxStatus.FAILED)))))
                .thenAnswer(invocation -> claimAll(List.of(outboxEvent)));
        when(outboxStore.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(outboxStore.findByIdForUpdate(1L)).thenReturn(Optional.of(outboxEvent));
        when(serializer.deserialize(new SerializedEvent("event-type", "payload", 1, "{}"))).thenReturn(payload);

        delivery.pollOnce();

        verify(dispatcher).dispatch(payload);
        assertEquals(OutboxStatus.PUBLISHED, outboxEvent.getStatus());
        assertEquals(1, outboxEvent.getAttemptCount());
        assertNotNull(outboxEvent.getPublishedAt());
        assertNull(outboxEvent.getClaimedAt());
        assertNull(outboxEvent.getLastError());
    }

    @Test
    void processAvailableEvents_whenDispatchFails_marksEventFailedForRetry() {
        Event payload = new StockReceivedEvent(id("inventory-1"), "SKU-1", 10, id("location-1"), LocalDateTime.now());
        InventoryOutboxEvent outboxEvent = pendingEvent(1L, "event-type", "payload");

        when(outboxStore.claimBatch(argThat(request -> request != null
                && request.limit() == 10
                && request.retryableStatuses().equals(List.of(OutboxStatus.NEW, OutboxStatus.FAILED)))))
                .thenAnswer(invocation -> claimAll(List.of(outboxEvent)));
        when(outboxStore.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(outboxStore.findByIdForUpdate(1L)).thenReturn(Optional.of(outboxEvent));
        when(serializer.deserialize(new SerializedEvent("event-type", "payload", 1, "{}"))).thenReturn(payload);
        doThrow(new IllegalStateException("dispatcher failed")).when(dispatcher).dispatch(payload);

        delivery.pollOnce();

        assertEquals(OutboxStatus.FAILED, outboxEvent.getStatus());
        assertEquals(1, outboxEvent.getAttemptCount());
        assertNull(outboxEvent.getClaimedAt());
        assertEquals("dispatcher failed", outboxEvent.getLastError());
        assertTrue(outboxEvent.getAvailableAt().isAfter(outboxEvent.getOccurredAt()));
    }

    @Test
    void processAvailableEvents_whenClaimTokenChanges_skipsDispatch() {
        InventoryOutboxEvent pendingEvent = pendingEvent(1L, "event-type", "payload");
        InventoryOutboxEvent reclaimedEvent = pendingEvent(1L, "event-type", "payload");
        reclaimedEvent.markProcessing(Instant.now(), "different-claim-token");

        when(outboxStore.claimBatch(argThat(request -> request != null
                && request.limit() == 10
                && request.retryableStatuses().equals(List.of(OutboxStatus.NEW, OutboxStatus.FAILED)))))
                .thenAnswer(invocation -> claimAll(List.of(pendingEvent)));
        when(outboxStore.findById(1L)).thenReturn(Optional.of(reclaimedEvent));

        delivery.pollOnce();

        verifyNoInteractions(dispatcher);
        assertEquals(OutboxStatus.PROCESSING, pendingEvent.getStatus());
    }

    @Test
    void cleanupPublishedEvents_deletesExpiredRows() {
        delivery.cleanupPublished();

        verify(outboxStore).deletePublishedOlderThan(any(Instant.class), eq(5000));
    }

    private static InventoryOutboxEvent pendingEvent(Long id, String eventType, String payload) {
        InventoryOutboxEvent outboxEvent = InventoryOutboxEvent.pending(
                UUID.randomUUID(),
                "InventoryItem",
                "inventory-1",
                new SerializedEvent(eventType, payload, 1, "{}"),
                Instant.now().minusSeconds(60)
        );
        ReflectionTestUtils.setField(outboxEvent, "id", id);
        return outboxEvent;
    }

    private static Id id(String value) {
        return new Id() {
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public boolean equals(Object other) {
                if (!(other instanceof Id id)) {
                    return false;
                }

                return Objects.equals(value, id.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(value);
            }

            @Override
            public String toString() {
                return value;
            }
        };
    }

    private static final class NoOpTransactionManager implements PlatformTransactionManager {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
        }

        @Override
        public void rollback(TransactionStatus status) {
        }
    }

    private static List<ClaimedOutboxEvent<Long>> claimAll(List<InventoryOutboxEvent> events) {
        return events.stream()
                .map(event -> {
                    String claimToken = "claim-" + event.getId();
                    event.markProcessing(Instant.now(), claimToken);
                    return new ClaimedOutboxEvent<>(event.getId(), claimToken);
                })
                .toList();
    }
}
