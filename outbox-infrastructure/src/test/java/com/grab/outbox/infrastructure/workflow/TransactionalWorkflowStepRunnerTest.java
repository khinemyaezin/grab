package com.grab.outbox.infrastructure.workflow;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.outbox.OutboxDeliveryContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionalWorkflowStepRunnerTest {

    private RecordingProducer producer;
    private InMemoryInbox inbox;
    private TestRunner runner;

    @BeforeEach
    void setUp() {
        producer = new RecordingProducer();
        inbox = new InMemoryInbox();
        runner = new TestRunner(producer, inbox, new InboxTransactionManager(inbox));
    }

    @Test
    void redeliveredStep_runsWorkAndEmitsSignalsOnlyOnce() {
        UUID eventId = UUID.randomUUID();
        AtomicInteger executions = new AtomicInteger();

        for (int delivery = 0; delivery < 2; delivery++) {
            OutboxDeliveryContext.run(eventId, () -> runner.runStep("wf-1", () -> {
                executions.incrementAndGet();
                return List.of(new Signal("done"));
            }, exception -> List.of(new Signal("failed"))));
        }

        assertEquals(1, executions.get());
        assertEquals(List.of(new Signal("done")), producer.events);
        assertTrue(inbox.keys.contains("test.workflow-step:" + eventId));
    }

    @Test
    void failedStep_emitsFailureSignalOnceAcrossRedeliveries() {
        UUID eventId = UUID.randomUUID();

        for (int delivery = 0; delivery < 2; delivery++) {
            OutboxDeliveryContext.run(eventId, () -> runner.runStep("wf-1", () -> {
                throw new IllegalStateException("boom");
            }, exception -> List.of(new Signal("failed: " + exception.getMessage()))));
        }

        assertEquals(List.of(new Signal("failed: boom")), producer.events);
    }

    @Test
    void stepOutsideOutboxDelivery_isNotDeduplicated() {
        AtomicInteger executions = new AtomicInteger();

        runner.runStep("wf-1", () -> {
            executions.incrementAndGet();
            return List.of();
        }, exception -> List.of());
        runner.runStep("wf-1", () -> {
            executions.incrementAndGet();
            return List.of();
        }, exception -> List.of());

        assertEquals(2, executions.get());
        assertTrue(inbox.keys.isEmpty());
    }

    private record Signal(String name) implements Event {
    }

    private static final class TestRunner extends TransactionalWorkflowStepRunner {
        private TestRunner(DomainEventProducer producer, ProcessedEventRepository inbox,
                           PlatformTransactionManager transactionManager) {
            super("test", producer, transactionManager, inbox);
        }
    }

    private static final class RecordingProducer implements DomainEventProducer {
        private final List<Event> events = new ArrayList<>();

        @Override
        public void produce(String aggregateType, String aggregateId, List<Event> produced) {
            events.addAll(produced);
        }
    }

    /**
     * Inbox rows become visible only when the surrounding (fake) transaction commits, and are discarded on
     * rollback, mirroring the real {@code processed_event} insert inside the step transaction.
     */
    private static final class InMemoryInbox implements ProcessedEventRepository {
        private final Set<String> keys = new HashSet<>();
        private final Set<String> staged = new HashSet<>();

        @Override
        public boolean markProcessed(String consumer, UUID eventId) {
            String key = consumer + ":" + eventId;
            if (keys.contains(key) || staged.contains(key)) {
                return false;
            }
            staged.add(key);
            return true;
        }

        @Override
        public int deleteProcessedBefore(Instant cutoff, int limit) {
            return 0;
        }

        void commit() {
            keys.addAll(staged);
            staged.clear();
        }

        void rollback() {
            staged.clear();
        }
    }

    private static final class InboxTransactionManager implements PlatformTransactionManager {
        private final InMemoryInbox inbox;

        private InboxTransactionManager(InMemoryInbox inbox) {
            this.inbox = inbox;
        }

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            inbox.commit();
        }

        @Override
        public void rollback(TransactionStatus status) {
            inbox.rollback();
        }
    }
}
