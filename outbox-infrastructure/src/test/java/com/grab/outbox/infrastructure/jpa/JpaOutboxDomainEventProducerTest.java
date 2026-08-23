package com.grab.outbox.infrastructure.jpa;

import com.grab.framework.domain.Event;
import com.grab.framework.logger.slf4j.TraceContext;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.OutboxRowFactory;
import com.grab.outbox.infrastructure.OutboxStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.LocalDateTime;
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
    private JpaOutboxDomainEventProducer<StubOutboxEvent> producer;

    @BeforeEach
    void setUp() {
        outboxStore = mock(OutboxStore.class);
        serializer = mock(OutboxEventSerializer.class);
        rowFactory = mock(OutboxRowFactory.class);
        producer = new JpaOutboxDomainEventProducer<>(outboxStore, serializer, rowFactory);
    }

    @AfterEach
    void clearMdc() {
        TraceContext.clear();
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

    private record DummyEvent() implements Event {
    }

    static final class StubOutboxEvent implements OutboxEntry<Long> {
        private OutboxStatus status = OutboxStatus.NEW;
        private String claimToken;

        @Override
        public Long getId() {
            return 1L;
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
