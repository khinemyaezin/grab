package com.catalog.infrastructure.outbox;

import com.catalog.domain.event.ProductUpdatedEvent;
import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.OutboxStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogOutboxEventProducerTest {

    private OutboxStore<CatalogOutboxEvent, Long> outboxStore;
    private OutboxEventSerializer serializer;
    private CatalogOutboxEventProducer producer;

    @BeforeEach
    void setUp() {
        outboxStore = mock(OutboxStore.class);
        serializer = mock(OutboxEventSerializer.class);
        producer = new CatalogOutboxEventProducer(outboxStore, serializer);
    }

    @Test
    void produce_persistsOutboxRowsForEachEvent() {
        Event firstEvent = new ProductUpdatedEvent(id("product-1"), "New name", id("category-1"));
        Event secondEvent = new ProductUpdatedEvent(id("product-1"), "Newer name", id("category-2"));

        when(serializer.serialize(firstEvent)).thenReturn(new SerializedEvent("event-1", "payload-1", 1, "{}"));
        when(serializer.serialize(secondEvent)).thenReturn(new SerializedEvent("event-2", "payload-2", 1, "{}"));

        producer.produce("Product", "product-1", List.of(firstEvent, secondEvent));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CatalogOutboxEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(outboxStore).saveAll(eventsCaptor.capture());

        List<CatalogOutboxEvent> outboxEvents = eventsCaptor.getValue();
        assertEquals(2, outboxEvents.size());
        assertEquals("Product", outboxEvents.getFirst().getAggregateType());
        assertEquals("product-1", outboxEvents.getFirst().getAggregateId());
        assertEquals("event-1", outboxEvents.getFirst().getEventType());
        assertEquals(1, outboxEvents.getFirst().getEventVersion());
        assertEquals("{}", outboxEvents.getFirst().getHeaders());
        assertEquals("payload-1", outboxEvents.getFirst().getPayload());
        assertNotNull(outboxEvents.getFirst().getOccurredAt());
        assertNotNull(outboxEvents.getFirst().getAvailableAt());
        assertEquals("event-2", outboxEvents.get(1).getEventType());
        assertEquals("payload-2", outboxEvents.get(1).getPayload());
    }

    @Test
    void produce_withEmptyEvents_skipsPersistence() {
        producer.produce("Product", "product-1", List.of());

        verify(outboxStore, never()).saveAll(anyList());
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
}
