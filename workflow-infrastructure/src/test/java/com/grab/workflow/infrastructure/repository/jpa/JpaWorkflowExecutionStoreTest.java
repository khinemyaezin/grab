package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.workflow.infrastructure.adapter.JpaWorkflowExecutionStore;
import com.grab.workflow.infrastructure.adapter.JpaWorkflowStartReceiptStore;
import com.grab.framework.workflow.ExecutionStatus;
import com.grab.framework.workflow.NodeExecution;
import com.grab.framework.workflow.TaskNode;
import com.grab.framework.workflow.WorkflowStartReceipt;
import com.grab.framework.workflow.WorkflowConcurrencyException;
import jakarta.persistence.EntityManager;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowFlow;
import com.grab.framework.workflow.WorkflowIdentity;
import com.grab.framework.workflow.WorkflowOperation;
import com.grab.framework.workflow.WorkflowStep;
import com.grab.framework.workflow.WorkflowTimer;
import com.grab.framework.workflow.WorkflowTimerIntent;
import com.grab.framework.workflow.TimerPurpose;
import com.grab.framework.workflow.WorkflowExecutionState;
import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowDispatch;
import com.grab.framework.workflow.WorkflowFailureClassification;
import com.grab.framework.workflow.WorkflowSignal;
import com.grab.framework.workflow.WorkflowSignalOutcome;
import com.grab.framework.workflow.WorkflowTransition;
import com.grab.framework.workflow.WorkflowTransitionEngine;
import com.grab.framework.workflow.CompensationDefinition;
import com.grab.framework.workflow.CompensationInput;
import com.grab.framework.workflow.WorkflowStoredExecution;
import com.grab.workflow.infrastructure.serialization.JacksonWorkflowPayloadCodec;
import com.grab.workflow.infrastructure.entity.WorkflowExecutionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import com.grab.workflow.infrastructure.serialization.JacksonWorkflowFingerprint;

import static com.grab.framework.workflow.WorkflowDsl.bind;
import static com.grab.framework.workflow.WorkflowDsl.flow;
import static com.grab.framework.workflow.WorkflowDsl.join;
import static com.grab.framework.workflow.WorkflowDsl.map;
import static com.grab.framework.workflow.WorkflowDsl.parallel;
import static com.grab.framework.workflow.WorkflowDsl.sequence;
import static com.grab.framework.workflow.WorkflowDsl.task;
import static com.grab.framework.workflow.WorkflowDsl.workflow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = JpaWorkflowExecutionStoreTest.JpaTestConfig.class)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class JpaWorkflowExecutionStoreTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    private WorkflowExecutionJpaRepository executionRepository;

    @Autowired
    private WorkflowNodeExecutionJpaRepository nodeRepository;

    @Autowired
    private WorkflowStartReceiptJpaRepository startReceiptRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void restoresTypedJoinAndBoundRetryWithoutReevaluatingCompletedCallbacks() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AtomicInteger joins = new AtomicInteger();
        AtomicInteger requests = new AtomicInteger();
        WorkflowStep<String, Reservation> left = map("left", String.class, Reservation.class, Reservation::new);
        WorkflowStep<String, Integer> right = map("right", String.class, Integer.class, String::length);
        WorkflowStep<String, Joined> join = join("join", left, right, Joined.class, (reservation, length) -> {
            joins.incrementAndGet();
            return new Joined(List.of(reservation), length);
        });
        WorkflowOperation<Reservation, Reservation> operation =
                WorkflowOperation.mutation("owner.write", Reservation.class, Reservation.class);
        CompensationDefinition reverse = CompensationDefinition.of("owner.reverse", List.of());
        Optional<CompensationDefinition> compensation = Optional.of(reverse);
        WorkflowStep<Joined, Joined> write = bind("bound", Joined.class, Joined.class, operation,
                input -> {
                    requests.incrementAndGet();
                    return input.reservations().getFirst();
                }, (input, receipt) -> new Joined(List.of(receipt), input.length()), compensation);
        WorkflowIdentity identity = new WorkflowIdentity("typed-workflow", 2, 2);
        WorkflowFlow<String, String> start = flow(String.class);
        WorkflowFlow<String, Joined> steps = start.then(join).then(write);
        WorkflowDefinition<String, Joined> definition = steps.build(identity, "root");
        WorkflowTransitionEngine engine = new WorkflowTransitionEngine(new JacksonWorkflowFingerprint());

        WorkflowTransition issued = engine.start(definition, "typed-execution", "scope", 2, "sku", now);
        WorkflowDispatch original = issued.dispatches().getFirst();
        WorkflowSignal failure = new WorkflowSignal("retry", "typed-execution", original.nodeId(),
                original.logicalOperationId(), original.attemptId(), original.attempt(), WorkflowPhase.FORWARD,
                WorkflowSignalOutcome.FAILED, null, WorkflowFailureClassification.RETRYABLE, null, null, 2, now);
        WorkflowTransition waiting = engine.accept(definition, issued.state(), failure, now);
        WorkflowTimerIntent retry = waiting.timers().stream()
                .filter(timer -> timer.purpose() == TimerPurpose.RETRY).findFirst().orElseThrow();
        JpaWorkflowExecutionStore store = new JpaWorkflowExecutionStore(
                executionRepository, nodeRepository, startReceiptRepository, new JacksonWorkflowPayloadCodec());
        WorkflowStoredExecution record = new WorkflowStoredExecution("scope", 2, "typed-key", waiting.state(), now, now, 0);
        store.create(record);
        JpaWorkflowStartReceiptStore receipts = new JpaWorkflowStartReceiptStore(startReceiptRepository, entityManager);
        WorkflowStartReceipt receipt = new WorkflowStartReceipt("scope", "typed-key", "typed-workflow",
                2, 2, "hash", "typed-execution", now);
        receipts.insert(receipt);
        entityManager.flush();
        entityManager.clear();

        WorkflowStoredExecution restored = store.find("typed-execution", definition).orElseThrow();
        Joined joined = assertInstanceOf(Joined.class, restored.state().node("join").output());
        assertInstanceOf(Reservation.class, joined.reservations().getFirst());
        WorkflowTimer due = new WorkflowTimer(retry.nodeId(), retry.logicalOperationId(), retry.purpose(),
                retry.generation(), retry.dueAt(), retry.phase());
        WorkflowTransition retried = engine.onTimer(definition, restored.state(), due, retry.dueAt());
        WorkflowDispatch dispatch = retried.dispatches().getFirst();
        assertEquals(original.input(), dispatch.input());
        assertEquals(1, joins.get());
        assertEquals(1, requests.get());

        Reservation written = new Reservation("assigned");
        WorkflowSignal success = new WorkflowSignal("success", "typed-execution", dispatch.nodeId(),
                dispatch.logicalOperationId(), dispatch.attemptId(), dispatch.attempt(), WorkflowPhase.FORWARD,
                WorkflowSignalOutcome.SUCCEEDED, written, WorkflowFailureClassification.UNCLASSIFIED, null, null, 2, now);
        WorkflowTransition finished = engine.accept(definition, retried.state(), success, now);
        WorkflowStoredExecution completed = new WorkflowStoredExecution("scope", 2, "typed-key",
                finished.state(), now, now, restored.version());
        store.save(completed);
        entityManager.flush();
        entityManager.clear();

        WorkflowStoredExecution result = store.find("typed-execution", definition).orElseThrow();
        assertEquals(new Joined(List.of(written), 3), result.state().result());
        assertEquals(1, joins.get());
        assertEquals(1, requests.get());
    }

    private record Joined(List<Reservation> reservations, int length) {
    }

    @Test
    void savesAndRestoresStateThroughPostgres() {
        JpaWorkflowExecutionStore store = new JpaWorkflowExecutionStore(
                executionRepository,
                nodeRepository,
                startReceiptRepository,
                new JacksonWorkflowPayloadCodec()
        );
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        NodeExecution node = NodeExecution.pending("task")
                .active("sku")
                .dispatch("operation", "attempt-1", "sku", createdAt.plusSeconds(10))
                .succeed("ok", "fingerprint");
        WorkflowExecutionState state = new WorkflowExecutionState(
                "execution-1",
                "workflow",
                1,
                "scope",
                1,
                ExecutionStatus.COMPLETED,
                "sku",
                Map.of("task", node),
                Map.of(),
                Map.of("task", "ok"),
                Map.of("route", "primary"),
                Map.of("message-1", "fingerprint"),
                false,
                null,
                "ok"
        );
        WorkflowStoredExecution stored = new WorkflowStoredExecution(
                "scope", 1, "idempotency-1", state, createdAt, createdAt.plusSeconds(2), 0
        );
        WorkflowStoredExecution created = store.create(stored);
        JpaWorkflowStartReceiptStore receipts = new JpaWorkflowStartReceiptStore(startReceiptRepository, entityManager);
        WorkflowStartReceipt receipt = new WorkflowStartReceipt("scope", "idempotency-1", "workflow",
                1, 2, "hash", "execution-1", createdAt);
        receipts.insert(receipt);
        store.save(created);
        entityManager.flush();
        entityManager.clear();
        assertThrows(WorkflowConcurrencyException.class, () -> store.save(created));
        TaskNode<String, String> task = new TaskNode<>("task", String.class, String.class, "operation");
        WorkflowDefinition<String, String> definition = WorkflowDefinition.of(
                "workflow", 1, String.class, String.class, task, "task"
        );

        WorkflowStoredExecution restored = store.find("execution-1", definition).orElseThrow();

        assertEquals("sku", restored.state().input());
        assertEquals("ok", restored.state().result());
        assertEquals("ok", restored.state().node("task").output());
        assertEquals("primary", restored.state().choices().get("route"));
        assertEquals("fingerprint", restored.state().acceptedSignals().get("message-1"));
        assertEquals(WorkflowPhase.FORWARD, restored.state().node("task").phase());
    }

    @Test
    void find_nestedJoinAndRetryInput_reconstructsRecordValuesAfterClear() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        WorkflowTransitionEngine engine = new WorkflowTransitionEngine(new JacksonWorkflowFingerprint());
        TaskNode<String, Reservation> first = task("first", String.class, Reservation.class, "reserve");
        first = first.withCompensation(CompensationDefinition.of("release", List.of()));
        TaskNode<String, Reservation> second = task("second", String.class, Reservation.class, "price");
        second = second.withCompensation(CompensationDefinition.of("unprice", List.of()));
        TaskNode<List, String> next = task("next", List.class, String.class, "publish");
        WorkflowDefinition<String, String> definition = workflow("nested", 1, String.class, String.class)
                .root(sequence("root", parallel("outer", parallel("inner", first), second), next))
                .build();
        WorkflowTransition started = engine.start(definition, "nested-execution", "scope", 1, "sku", now);
        WorkflowDispatch firstDispatch = started.dispatches().getFirst();
        WorkflowDispatch secondDispatch = started.dispatches().getLast();
        Reservation boxed = new Reservation("box");
        Reservation wrapped = new Reservation("wrap");
        WorkflowTransition one = engine.accept(definition, started.state(), outcome(firstDispatch, boxed, now), now);
        WorkflowTransition two = engine.accept(definition, one.state(), outcome(secondDispatch, wrapped, now), now);
        WorkflowDispatch nextDispatch = two.dispatches().getFirst();
        WorkflowSignal failure = new WorkflowSignal("failure", "nested-execution", "next",
                nextDispatch.logicalOperationId(), nextDispatch.attemptId(), 1, WorkflowPhase.FORWARD,
                WorkflowSignalOutcome.FAILED, null, WorkflowFailureClassification.RETRYABLE, null, "temporary", 1, now);
        WorkflowTransition waiting = engine.accept(definition, two.state(), failure, now);
        JpaWorkflowExecutionStore store = new JpaWorkflowExecutionStore(
                executionRepository, nodeRepository, startReceiptRepository, new JacksonWorkflowPayloadCodec());
        WorkflowStoredExecution execution = new WorkflowStoredExecution("scope", 1, "nested-key",
                waiting.state(), now, now, 0);
        store.create(execution);
        JpaWorkflowStartReceiptStore receipts = new JpaWorkflowStartReceiptStore(startReceiptRepository, entityManager);
        receipts.insert(new WorkflowStartReceipt("scope", "nested-key", "nested", 1, 2, "hash", "nested-execution", now));
        entityManager.flush();
        entityManager.clear();

        WorkflowStoredExecution restored = store.find("nested-execution", definition).orElseThrow();

        List<Object> expected = List.of(List.of(boxed), wrapped);
        assertEquals(expected, restored.state().node("next").input());
        assertEquals(expected, restored.state().outputs().get("outer"));
        WorkflowTransition cancelling = engine.cancel(definition, restored.state(), now);
        WorkflowStoredExecution compensating = new WorkflowStoredExecution("scope", 1, "nested-key",
                cancelling.state(), now, now, restored.version());
        store.save(compensating);
        entityManager.flush();
        entityManager.clear();

        WorkflowStoredExecution withCompensation = store.find("nested-execution", definition).orElseThrow();
        NodeExecution compensation = withCompensation.state().compensationNodes().get("comp:first");
        CompensationInput<?, ?> input = assertInstanceOf(CompensationInput.class, compensation.input());
        assertEquals(boxed, input.output());
    }

    private WorkflowSignal outcome(WorkflowDispatch dispatch, Reservation payload, Instant now) {
        return new WorkflowSignal("message-" + dispatch.nodeId(), "nested-execution", dispatch.nodeId(),
                dispatch.logicalOperationId(), dispatch.attemptId(), dispatch.attempt(), dispatch.phase(),
                WorkflowSignalOutcome.SUCCEEDED, payload, WorkflowFailureClassification.UNCLASSIFIED, null, null, 1, now);
    }

    record Reservation(String name) {
    }

    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableJpaRepositories(basePackageClasses = WorkflowExecutionJpaRepository.class)
    @EntityScan(basePackageClasses = WorkflowExecutionEntity.class)
    static class JpaTestConfig {
    }
}
