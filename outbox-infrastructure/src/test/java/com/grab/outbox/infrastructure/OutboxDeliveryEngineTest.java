package com.grab.outbox.infrastructure;

import com.grab.framework.domain.Event;
import com.grab.framework.logger.slf4j.TraceContext;
import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxDeliveryContext;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxPoisonEventException;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.outbox.OutboxRetryPolicy;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.adapter.in.relay.DualQueueOutboxRelay;
import com.grab.outbox.infrastructure.adapter.in.relay.OutboxRelays;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.core.port.out.OutboxClaimRequest;
import com.grab.outbox.infrastructure.core.port.out.OutboxObserver;
import com.grab.outbox.infrastructure.core.port.out.OutboxTransactions;
import com.grab.outbox.infrastructure.core.service.OutboxDeliveryEngine;
import com.grab.outbox.infrastructure.core.service.OutboxOutcomeRecorder;
import com.grab.outbox.infrastructure.core.service.OutboxRetentionCleaner;
import com.grab.outbox.infrastructure.module.OutboxProcessorSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxDeliveryEngineTest {

    private static final Instant NOW = Instant.parse("2026-11-13T10:00:00Z");

    private JpaOutboxStore<StubOutboxEvent, Long> outboxStore;
    private OutboxEventSerializer serializer;
    private OutboxEventDispatcher dispatcher;
    private RecordingObserver observer;
    private OutboxDeliveryEngine<StubOutboxEvent, Long> processor;

    @BeforeEach
    void setUp() {
        outboxStore = mock(JpaOutboxStore.class);
        serializer = mock(OutboxEventSerializer.class);
        dispatcher = mock(OutboxEventDispatcher.class);
        observer = new RecordingObserver();
        processor = processor(OutboxRelay.noop(), 3);
    }

    @AfterEach
    void tearDown() {
        TraceContext.clear();
        processor.shutdown();
    }

    @Test
    void processAvailableEvents_restoresTraceIdFromHeadersDuringDispatch() {
        StubOutboxEvent outboxEvent = new StubOutboxEvent(
                1L,
                "{\"contentType\":\"application/json\",\"traceId\":\"outbox-trace\"}"
        );
        Event payload = givenClaimable(outboxEvent);
        AtomicReference<String> observed = new AtomicReference<>();
        doAnswer(invocation -> {
            observed.set(TraceContext.current());
            return null;
        }).when(dispatcher).dispatch(payload);

        processor.pollOnce();

        verify(dispatcher).dispatch(payload);
        assertEquals("outbox-trace", observed.get());
        assertEquals(OutboxStatus.PUBLISHED, outboxEvent.getStatus());
        assertEquals(1, observer.published.get());
        assertNull(TraceContext.current());
    }

    @Test
    void processAvailableEvents_generatesTraceIdWhenHeadersLackOne() {
        StubOutboxEvent outboxEvent = new StubOutboxEvent(1L, "{}");
        Event payload = givenClaimable(outboxEvent);
        AtomicReference<String> observed = new AtomicReference<>();
        doAnswer(invocation -> {
            observed.set(TraceContext.current());
            return null;
        }).when(dispatcher).dispatch(payload);

        processor.pollOnce();

        assertNotNull(observed.get());
        UUID.fromString(observed.get());
        assertNull(TraceContext.current());
    }

    @Test
    void dispatch_exposesEventIdToListeners() {
        StubOutboxEvent outboxEvent = new StubOutboxEvent(1L, "{}");
        Event payload = givenClaimable(outboxEvent);
        AtomicReference<UUID> observed = new AtomicReference<>();
        doAnswer(invocation -> {
            observed.set(OutboxDeliveryContext.currentEventId().orElse(null));
            return null;
        }).when(dispatcher).dispatch(payload);

        processor.pollOnce();

        assertEquals(outboxEvent.getEventId(), observed.get());
        assertTrue(OutboxDeliveryContext.currentEventId().isEmpty());
    }

    @Test
    void listenerFailure_marksFailedWithExponentialBackoff() {
        StubOutboxEvent outboxEvent = new StubOutboxEvent(1L, "{}").withAttempts(1);
        Event payload = givenClaimable(outboxEvent);
        doThrow(new IllegalStateException("listener down")).when(dispatcher).dispatch(payload);

        processor.pollOnce();

        // attempt 2 of 3 -> base 30s * 2^(2-1) = 60s (jitter disabled in this test)
        assertEquals(OutboxStatus.FAILED, outboxEvent.getStatus());
        assertEquals(NOW.plusSeconds(60), outboxEvent.getAvailableAt());
        assertEquals("listener down", outboxEvent.lastError());
        assertNull(outboxEvent.getClaimToken());
        assertEquals(1, observer.failed.get());
    }

    @Test
    void listenerFailure_onLastAttempt_deadLetters() {
        StubOutboxEvent outboxEvent = new StubOutboxEvent(1L, "{}").withAttempts(2);
        Event payload = givenClaimable(outboxEvent);
        doThrow(new IllegalStateException("still down")).when(dispatcher).dispatch(payload);

        processor.pollOnce();

        assertEquals(3, outboxEvent.getAttemptCount());
        assertEquals(OutboxStatus.DEAD, outboxEvent.getStatus());
        assertEquals(1, observer.dead.get());
    }

    @Test
    void poisonPayload_deadLettersImmediately() {
        StubOutboxEvent outboxEvent = new StubOutboxEvent(1L, "{}");
        givenClaimable(outboxEvent);
        when(serializer.deserialize(any(SerializedEvent.class)))
                .thenThrow(new OutboxPoisonEventException("unknown type"));

        processor.pollOnce();

        assertEquals(1, outboxEvent.getAttemptCount());
        assertEquals(OutboxStatus.DEAD, outboxEvent.getStatus());
        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    void claimLostDuringDispatch_doesNotOverwriteNewOwnersState() {
        StubOutboxEvent outboxEvent = new StubOutboxEvent(1L, "{}");
        Event payload = givenClaimable(outboxEvent);
        StubOutboxEvent reclaimedByOtherPod = new StubOutboxEvent(1L, "{}");
        reclaimedByOtherPod.markProcessing(NOW, "other-pod-token");
        when(outboxStore.findByIdForUpdate(1L)).thenReturn(Optional.of(reclaimedByOtherPod));

        processor.pollOnce();

        verify(dispatcher).dispatch(payload);
        assertEquals(OutboxStatus.PROCESSING, reclaimedByOtherPod.getStatus());
        assertEquals("other-pod-token", reclaimedByOtherPod.getClaimToken());
        assertEquals(1, observer.claimLost.get());
        assertEquals(0, observer.published.get());
    }

    @Test
    void claimLostBeforeDispatch_skipsDispatch() {
        StubOutboxEvent outboxEvent = new StubOutboxEvent(1L, "{}");
        givenClaimable(outboxEvent);
        StubOutboxEvent reclaimed = new StubOutboxEvent(1L, "{}");
        reclaimed.markProcessing(NOW, "other");
        when(outboxStore.findById(1L)).thenReturn(Optional.of(reclaimed));

        processor.pollOnce();

        verify(dispatcher, never()).dispatch(any());
        assertEquals(1, observer.claimLost.get());
    }

    @Test
    void hotQueue_claimsOneAndPublishes() throws Exception {
        DualQueueOutboxRelay<Long> hotRelay = new DualQueueOutboxRelay<>("test", 1, 8, 8, Duration.ofSeconds(1));
        OutboxDeliveryEngine<StubOutboxEvent, Long> hotProcessor = processor(hotRelay, 3);
        try {
            StubOutboxEvent outboxEvent = new StubOutboxEvent(1L, "{}");
            Event payload = givenRow(outboxEvent);
            when(outboxStore.claimOne(eq(1L), any(Instant.class))).thenAnswer(invocation -> Optional.of(claim(outboxEvent)));
            CountDownLatch dispatched = new CountDownLatch(1);
            doAnswer(invocation -> {
                dispatched.countDown();
                return null;
            }).when(dispatcher).dispatch(payload);

            assertTrue(hotRelay.enqueueHot(1L));
            assertTrue(dispatched.await(2, TimeUnit.SECONDS));
            awaitStatus(outboxEvent, OutboxStatus.PUBLISHED);
        } finally {
            hotProcessor.shutdown();
        }
    }

    @Test
    void processAvailableEvents_withRelay_enqueuesColdAndPublishes() throws Exception {
        DualQueueOutboxRelay<Long> coldRelay = new DualQueueOutboxRelay<>("test-cold", 1, 8, 8, Duration.ofSeconds(1));
        OutboxDeliveryEngine<StubOutboxEvent, Long> coldProcessor = processor(coldRelay, 3);
        try {
            StubOutboxEvent outboxEvent = new StubOutboxEvent(2L, "{}");
            Event payload = givenRow(outboxEvent);
            when(outboxStore.claimBatch(any())).thenAnswer(invocation -> List.of(claim(outboxEvent)));
            CountDownLatch dispatched = new CountDownLatch(1);
            doAnswer(invocation -> {
                dispatched.countDown();
                return null;
            }).when(dispatcher).dispatch(payload);

            coldProcessor.pollOnce();
            assertTrue(dispatched.await(2, TimeUnit.SECONDS));
            awaitStatus(outboxEvent, OutboxStatus.PUBLISHED);
        } finally {
            coldProcessor.shutdown();
        }
    }

    @Test
    void destroy_releasesUndispatchedColdClaims() {
        StubOutboxEvent outboxEvent = new StubOutboxEvent(5L, "{}");
        outboxEvent.markProcessing(NOW, "token-5");
        when(outboxStore.findByIdForUpdate(5L)).thenReturn(Optional.of(outboxEvent));
        OutboxRelay<Long> relay = new StoppedRelay(List.of(new ClaimedOutboxEvent<>(5L, "token-5")));
        OutboxDeliveryEngine<StubOutboxEvent, Long> stopping = processor(relay, 3);

        stopping.shutdown();

        assertEquals(OutboxStatus.FAILED, outboxEvent.getStatus());
        assertEquals(NOW, outboxEvent.getAvailableAt());
        assertEquals(0, outboxEvent.getAttemptCount());
        assertEquals(1, observer.released.get());
    }

    @Test
    void cleanup_deletesInBatchesUntilExhausted() {
        when(outboxStore.deletePublishedOlderThan(any(Instant.class), anyInt())).thenReturn(2, 2, 1);
        OutboxDeliveryEngine<StubOutboxEvent, Long> batching = engine(OutboxRelay.noop(), settings(3, 2));

        batching.cleanupPublished();

        verify(outboxStore, org.mockito.Mockito.times(3))
                .deletePublishedOlderThan(eq(NOW.minus(Duration.ofDays(7))), eq(2));
        assertEquals(5, observer.cleaned.get());
    }

    private Event givenClaimable(StubOutboxEvent outboxEvent) {
        Event payload = givenRow(outboxEvent);
        OutboxClaimRequest expected = new OutboxClaimRequest(
                List.of(OutboxStatus.NEW, OutboxStatus.FAILED),
                NOW,
                NOW.minus(Duration.ofMinutes(2)),
                10
        );
        when(outboxStore.claimBatch(expected)).thenAnswer(invocation -> List.of(claim(outboxEvent)));
        return payload;
    }

    private static ClaimedOutboxEvent<Long> claim(StubOutboxEvent outboxEvent) {
        String claimToken = "claim-" + outboxEvent.getId();
        outboxEvent.markProcessing(NOW, claimToken);
        return new ClaimedOutboxEvent<>(outboxEvent.getId(), claimToken);
    }

    private Event givenRow(StubOutboxEvent outboxEvent) {
        Event payload = new DummyEvent();
        when(outboxStore.findById(outboxEvent.getId())).thenReturn(Optional.of(outboxEvent));
        when(outboxStore.findByIdForUpdate(outboxEvent.getId())).thenReturn(Optional.of(outboxEvent));
        when(serializer.deserialize(new SerializedEvent("type", "payload", 1, outboxEvent.getHeaders())))
                .thenReturn(payload);
        return payload;
    }

    private OutboxDeliveryEngine<StubOutboxEvent, Long> processor(OutboxRelay<Long> relay, int maxAttempts) {
        return engine(relay, settings(maxAttempts, 5000));
    }

    private OutboxDeliveryEngine<StubOutboxEvent, Long> engine(OutboxRelay<Long> relay, OutboxProcessorSettings settings) {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        OutboxTransactions transactions = new InMemoryTransactions();
        OutboxRetryPolicy retryPolicy = new OutboxRetryPolicy(
                settings.retryDelay(), settings.maxBackoff(), settings.maxAttempts(), 0d, () -> 0.5d);
        OutboxOutcomeRecorder<StubOutboxEvent, Long> outcomes = new OutboxOutcomeRecorder<>(
                "test", outboxStore, transactions, retryPolicy, observer, settings.claimTimeout(), clock);
        OutboxRetentionCleaner cleaner = new OutboxRetentionCleaner("test", outboxStore, transactions, observer,
                settings.retention(), settings.cleanupBatchSize(), clock);
        OutboxDeliveryEngine<StubOutboxEvent, Long> engine = new OutboxDeliveryEngine<>(
                outboxStore, serializer, dispatcher, transactions, relay, outcomes, cleaner::cleanupPublished,
                settings.batchSize(), settings.claimTimeout(), clock);
        OutboxRelays.bind(relay, engine);
        relay.start();
        return engine;
    }

    private static OutboxProcessorSettings settings(int maxAttempts, int cleanupBatchSize) {
        return new OutboxProcessorSettings(
                10,
                Duration.ofSeconds(30),
                Duration.ofMinutes(15),
                maxAttempts,
                Duration.ofMinutes(2),
                Duration.ofDays(7),
                cleanupBatchSize,
                Duration.ofMinutes(10),
                Duration.ofMinutes(4)
        );
    }

    private static void awaitStatus(StubOutboxEvent event, OutboxStatus expected) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (event.getStatus() != expected && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertEquals(expected, event.getStatus());
    }

    private record DummyEvent() implements Event {
    }

    private static final class RecordingObserver implements OutboxObserver {
        final AtomicInteger published = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();
        final AtomicInteger dead = new AtomicInteger();
        final AtomicInteger claimLost = new AtomicInteger();
        final AtomicInteger released = new AtomicInteger();
        final AtomicInteger cleaned = new AtomicInteger();

        @Override
        public void published(String module, String eventType, Duration deliveryLag) {
            published.incrementAndGet();
        }

        @Override
        public void failed(String module, String eventType, int attempt) {
            failed.incrementAndGet();
        }

        @Override
        public void dead(String module, String eventType) {
            dead.incrementAndGet();
        }

        @Override
        public void claimLost(String module) {
            claimLost.incrementAndGet();
        }

        @Override
        public void claimsReleased(String module, int count) {
            released.addAndGet(count);
        }

        @Override
        public void cleanedUp(String module, int deleted) {
            cleaned.addAndGet(deleted);
        }
    }

    private static final class StoppedRelay implements OutboxRelay<Long> {
        private final List<ClaimedOutboxEvent<Long>> pending;

        private StoppedRelay(List<ClaimedOutboxEvent<Long>> pending) {
            this.pending = pending;
        }

        @Override
        public boolean enqueueHot(Long id) {
            return false;
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
            return false;
        }

        @Override
        public void setHandler(com.grab.framework.outbox.OutboxWorkHandler<Long> handler) {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public List<ClaimedOutboxEvent<Long>> drainCold() {
            return pending;
        }
    }

    private static final class InMemoryTransactions implements OutboxTransactions {
        @Override
        public <R> R inNewTransaction(Supplier<R> work) {
            return work.get();
        }

        @Override
        public <R> R readOnly(Supplier<R> work) {
            return work.get();
        }
    }
}
