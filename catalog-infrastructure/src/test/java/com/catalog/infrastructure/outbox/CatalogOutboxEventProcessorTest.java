package com.catalog.infrastructure.outbox;

import com.catalog.domain.event.ProductUpdatedEvent;
import com.catalog.infrastructure.support.CatalogInfrastructureLoggerExtension;
import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.OutboxStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(CatalogInfrastructureLoggerExtension.class)
class CatalogOutboxEventProcessorTest {

    private OutboxStore<CatalogOutboxEvent, Long> outboxStore;
    private OutboxEventSerializer serializer;
    private OutboxEventDispatcher dispatcher;
    private CatalogOutboxEventProcessor processor;

    @BeforeEach
    void setUp() {
        outboxStore = mock(OutboxStore.class);
        serializer = mock(OutboxEventSerializer.class);
        dispatcher = mock(OutboxEventDispatcher.class);
        processor = new CatalogOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                new NoOpTransactionManager(),
                10,
                Duration.ofSeconds(30),
                Duration.ofMinutes(2),
                Duration.ofDays(7)
        );
    }

    @Test
    void processAvailableEvents_dispatchesAndMarksPublished() {
        Event payload = new ProductUpdatedEvent(id("product-1"), "Name", id("category-1"));
        CatalogOutboxEvent outboxEvent = pendingEvent(1L, "event-type", "payload");

        when(outboxStore.findBatchForProcessing(
                eq(List.of(OutboxStatus.NEW, OutboxStatus.FAILED)),
                eq(OutboxStatus.PROCESSING),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(10)
        )).thenReturn(List.of(outboxEvent));
        when(outboxStore.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(serializer.deserialize(new SerializedEvent("event-type", "payload", 1, "{}"))).thenReturn(payload);

        processor.processAvailableEventsOnSchedule();

        verify(dispatcher).dispatch(payload);
        assertEquals(OutboxStatus.PUBLISHED, outboxEvent.getStatus());
        assertEquals(1, outboxEvent.getAttemptCount());
        assertNotNull(outboxEvent.getPublishedAt());
        assertNull(outboxEvent.getClaimedAt());
        assertNull(outboxEvent.getLastError());
    }

    @Test
    void processAvailableEvents_whenDispatchFails_marksEventFailedForRetry() {
        Event payload = new ProductUpdatedEvent(id("product-1"), "Name", id("category-1"));
        CatalogOutboxEvent outboxEvent = pendingEvent(1L, "event-type", "payload");

        when(outboxStore.findBatchForProcessing(
                eq(List.of(OutboxStatus.NEW, OutboxStatus.FAILED)),
                eq(OutboxStatus.PROCESSING),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(10)
        )).thenReturn(List.of(outboxEvent));
        when(outboxStore.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(serializer.deserialize(new SerializedEvent("event-type", "payload", 1, "{}"))).thenReturn(payload);
        doThrow(new IllegalStateException("dispatcher failed")).when(dispatcher).dispatch(payload);

        processor.processAvailableEventsOnSchedule();

        assertEquals(OutboxStatus.FAILED, outboxEvent.getStatus());
        assertEquals(1, outboxEvent.getAttemptCount());
        assertNull(outboxEvent.getClaimedAt());
        assertEquals("dispatcher failed", outboxEvent.getLastError());
        assertTrue(outboxEvent.getAvailableAt().isAfter(outboxEvent.getOccurredAt()));
    }

    @Test
    void processAvailableEvents_whenClaimTokenChanges_skipsDispatch() {
        CatalogOutboxEvent pendingEvent = pendingEvent(1L, "event-type", "payload");
        CatalogOutboxEvent reclaimedEvent = pendingEvent(1L, "event-type", "payload");
        reclaimedEvent.markProcessing(LocalDateTime.now(), "different-claim-token");

        when(outboxStore.findBatchForProcessing(
                eq(List.of(OutboxStatus.NEW, OutboxStatus.FAILED)),
                eq(OutboxStatus.PROCESSING),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(10)
        )).thenReturn(List.of(pendingEvent));
        when(outboxStore.findById(1L)).thenReturn(Optional.of(reclaimedEvent));

        processor.processAvailableEventsOnSchedule();

        verifyNoInteractions(dispatcher);
        assertEquals(OutboxStatus.PROCESSING, pendingEvent.getStatus());
    }

    @Test
    void cleanupPublishedEvents_deletesExpiredRows() {
        processor.cleanupPublishedEventsOnSchedule();

        verify(outboxStore).deletePublishedOlderThan(any(LocalDateTime.class));
    }

    @Test
    void processAvailableEvents_supportsLegacyAndJsonRowsInSameBatch() {
        Event legacyPayload = new ProductUpdatedEvent(id("product-1"), "Legacy name", id("category-1"));
        Event jsonPayload = new ProductUpdatedEvent(id("product-1"), "Json name", id("category-2"));
        CatalogOutboxEvent legacyEvent = pendingEvent(1L, "event-type-legacy", "legacy-payload", "{}");
        CatalogOutboxEvent jsonEvent = pendingEvent(
                2L,
                "event-type-json",
                "{\"newName\":\"Json name\"}",
                "{\"contentType\":\"application/json\"}"
        );

        when(outboxStore.findBatchForProcessing(
                eq(List.of(OutboxStatus.NEW, OutboxStatus.FAILED)),
                eq(OutboxStatus.PROCESSING),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(10)
        )).thenReturn(List.of(legacyEvent, jsonEvent));
        when(outboxStore.findById(1L)).thenReturn(Optional.of(legacyEvent));
        when(outboxStore.findById(2L)).thenReturn(Optional.of(jsonEvent));
        when(serializer.deserialize(argThat(serialized ->
                serialized != null
                        && serialized.eventType().equals("event-type-legacy")
                        && serialized.payload().equals("legacy-payload")
                        && serialized.eventVersion() == 1
                        && serialized.headers().equals("{}")
        ))).thenReturn(legacyPayload);
        when(serializer.deserialize(argThat(serialized ->
                serialized != null
                        && serialized.eventType().equals("event-type-json")
                        && serialized.payload().equals("{\"newName\":\"Json name\"}")
                        && serialized.eventVersion() == 1
                        && serialized.headers().equals("{\"contentType\":\"application/json\"}")
        ))).thenReturn(jsonPayload);

        processor.processAvailableEventsOnSchedule();

        verify(dispatcher).dispatch(legacyPayload);
        verify(dispatcher).dispatch(jsonPayload);
        assertEquals(OutboxStatus.PUBLISHED, legacyEvent.getStatus());
        assertEquals(OutboxStatus.PUBLISHED, jsonEvent.getStatus());
    }

    private static CatalogOutboxEvent pendingEvent(Long id, String eventType, String payload) {
        return pendingEvent(id, eventType, payload, "{}");
    }

    private static CatalogOutboxEvent pendingEvent(Long id, String eventType, String payload, String headers) {
        CatalogOutboxEvent outboxEvent = CatalogOutboxEvent.pending(
                "Product",
                "product-1",
                new SerializedEvent(eventType, payload, 1, headers),
                LocalDateTime.now().minusMinutes(1)
        );
        outboxEvent.setId(id);
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
}
