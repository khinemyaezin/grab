package com.grab.outbox.infrastructure;

import com.grab.framework.domain.Event;
import com.grab.framework.logger.slf4j.TraceContext;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractOutboxProcessorTest {

    private OutboxStore<StubOutboxEvent, Long> outboxStore;
    private OutboxEventSerializer serializer;
    private OutboxEventDispatcher dispatcher;
    private TestProcessor processor;

    @BeforeEach
    void setUp() {
        outboxStore = mock(OutboxStore.class);
        serializer = mock(OutboxEventSerializer.class);
        dispatcher = mock(OutboxEventDispatcher.class);
        processor = new TestProcessor(
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

    @AfterEach
    void clearMdc() {
        TraceContext.clear();
    }

    @Test
    void processAvailableEvents_restoresTraceIdFromHeadersDuringDispatch() {
        Event payload = new DummyEvent();
        StubOutboxEvent outboxEvent = new StubOutboxEvent(
                1L,
                "{\"contentType\":\"application/json\",\"traceId\":\"outbox-trace\"}"
        );
        AtomicReference<String> observed = new AtomicReference<>();

        when(outboxStore.findBatchForProcessing(
                eq(List.of(OutboxStatus.NEW, OutboxStatus.FAILED)),
                eq(OutboxStatus.PROCESSING),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(10)
        )).thenReturn(List.of(outboxEvent));
        when(outboxStore.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(serializer.deserialize(new SerializedEvent("type", "payload", 1, outboxEvent.getHeaders())))
                .thenReturn(payload);
        org.mockito.Mockito.doAnswer(invocation -> {
            observed.set(TraceContext.current());
            return null;
        }).when(dispatcher).dispatch(payload);

        processor.process();

        verify(dispatcher).dispatch(payload);
        assertEquals("outbox-trace", observed.get());
        assertEquals(OutboxStatus.PUBLISHED, outboxEvent.getStatus());
        assertNull(TraceContext.current());
    }

    @Test
    void processAvailableEvents_generatesTraceIdWhenHeadersLackOne() {
        Event payload = new DummyEvent();
        StubOutboxEvent outboxEvent = new StubOutboxEvent(1L, "{}");
        AtomicReference<String> observed = new AtomicReference<>();

        when(outboxStore.findBatchForProcessing(
                eq(List.of(OutboxStatus.NEW, OutboxStatus.FAILED)),
                eq(OutboxStatus.PROCESSING),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(10)
        )).thenReturn(List.of(outboxEvent));
        when(outboxStore.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(serializer.deserialize(new SerializedEvent("type", "payload", 1, "{}")))
                .thenReturn(payload);
        org.mockito.Mockito.doAnswer(invocation -> {
            observed.set(TraceContext.current());
            return null;
        }).when(dispatcher).dispatch(payload);

        processor.process();

        assertNotNull(observed.get());
        UUID.fromString(observed.get());
        assertNull(TraceContext.current());
    }

    private static final class TestProcessor extends AbstractOutboxProcessor<StubOutboxEvent, Long> {
        private TestProcessor(
                OutboxStore<StubOutboxEvent, Long> outboxStore,
                OutboxEventSerializer serializer,
                OutboxEventDispatcher dispatcher,
                PlatformTransactionManager transactionManager,
                int batchSize,
                Duration retryDelay,
                Duration claimTimeout,
                Duration retention
        ) {
            super(outboxStore, serializer, dispatcher, transactionManager, batchSize, retryDelay, claimTimeout, retention);
        }

        void process() {
            processAvailableEvents();
        }
    }

    private record DummyEvent() implements Event {
    }

    static final class StubOutboxEvent implements OutboxEntry<Long> {
        private final Long id;
        private final String headers;
        private OutboxStatus status = OutboxStatus.NEW;
        private String claimToken;

        StubOutboxEvent(Long id, String headers) {
            this.id = id;
            this.headers = headers;
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
            return headers;
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
