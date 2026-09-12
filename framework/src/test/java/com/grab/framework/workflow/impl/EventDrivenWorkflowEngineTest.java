package com.grab.framework.workflow.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.CorrelationKey;
import com.grab.framework.workflow.InboundSignal;
import com.grab.framework.workflow.ProcessDefinition;
import com.grab.framework.workflow.StepDefinition;
import com.grab.framework.workflow.WorkflowDefinitionRegistry;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowLifecycleListener;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.WorkflowStepFailure;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventDrivenWorkflowEngineTest {

    private InMemoryWorkflowStore store;
    private List<Event> published;
    private List<WorkflowInstance> terminals;
    private EventDrivenWorkflowEngine engine;
    private ProcessDefinition<DemoContext> definition;
    private ProcessDefinition<DemoContext> forgetful;

    @BeforeEach
    void setUp() {
        store = new InMemoryWorkflowStore();
        published = new ArrayList<>();
        terminals = new ArrayList<>();
        definition = ProcessDefinition.of(
                "demo",
                DemoContext.class,
                new CreateStep(),
                new WaitViewStep(),
                new FinishStep()
        );
        forgetful = ProcessDefinition.of(
                "forgetful",
                DemoContext.class,
                new ForgetfulCompensationStep(),
                new WaitViewStep()
        );
        engine = new EventDrivenWorkflowEngine(
                store,
                new WorkflowDefinitionRegistry(List.of(definition, forgetful)),
                new WorkflowPayloadCodec(),
                (workflowId, events) -> published.addAll(events),
                new SequentialIdGenerator(),
                new WorkflowLifecycleListener() {
                    @Override
                    public void onTerminal(WorkflowInstance instance, Object context) {
                        terminals.add(instance);
                    }
                }
        );
    }

    @Test
    void start_shouldParkOnFirstStepAndPublishRequest() {
        WorkflowInstance instance = engine.start(definition, DemoContext.blank(), "idem-1");

        assertEquals(WorkflowStatus.WAITING_EXTERNAL, instance.status());
        assertEquals("create", instance.currentStep().orElseThrow());
        assertEquals(1, published.size());
        assertInstanceOf(RequestCreated.class, published.getFirst());
    }

    @Test
    void start_withSameIdempotencyKey_shouldReturnExisting() {
        WorkflowInstance first = engine.start(definition, DemoContext.blank(), "idem-1");
        published.clear();

        WorkflowInstance second = engine.start(definition, DemoContext.blank(), "idem-1");

        assertEquals(first.id(), second.id());
        assertTrue(published.isEmpty());
    }

    @Test
    void happyPath_shouldFanInThenComplete() {
        WorkflowInstance started = engine.start(definition, DemoContext.blank(), "idem-happy");
        published.clear();

        engine.onSignal(InboundSignal.completion(
                started.id(),
                "create",
                new Created("p-1"),
                "created:p-1"
        ));

        WorkflowInstance afterCreate = store.findById(started.id()).orElseThrow();
        assertEquals("wait-view", afterCreate.currentStep().orElseThrow());
        assertTrue(published.isEmpty());

        engine.onSignal(InboundSignal.correlated(
                CorrelationKey.product("p-1"),
                new Viewed("p-1", "SKU-1"),
                "viewed:p-1:SKU-1"
        ));

        WorkflowInstance completed = store.findById(started.id()).orElseThrow();
        assertEquals(WorkflowStatus.COMPLETED, completed.status());
        assertEquals(1, terminals.size());
        DemoContext context = new WorkflowPayloadCodec()
                .readTyped(completed.contextJson().orElseThrow(), DemoContext.class);
        assertEquals("p-1", context.productId());
        assertTrue(context.viewed());
    }

    @Test
    void correlatedSignal_shouldReachEveryWorkflowWaitingOnTheKey() {
        WorkflowInstance demoRun = engine.start(definition, DemoContext.blank(), "idem-demo");
        WorkflowInstance forgetfulRun = engine.start(forgetful, DemoContext.blank(), "idem-forgetful");
        engine.onSignal(InboundSignal.completion(
                demoRun.id(), "create", new Created("p-1"), "created:p-1"));
        engine.onSignal(InboundSignal.completion(
                forgetfulRun.id(), "create", new Created("p-1"), "created:p-1"));

        List<WorkflowInstance> reached = engine.onSignal(InboundSignal.correlated(
                CorrelationKey.product("p-1"),
                new Viewed("p-1", "SKU-1"),
                "viewed:p-1:SKU-1"
        ));

        assertEquals(2, reached.size());
        assertEquals(WorkflowStatus.COMPLETED, store.findById(demoRun.id()).orElseThrow().status());
        assertEquals(WorkflowStatus.COMPLETED, store.findById(forgetfulRun.id()).orElseThrow().status());
    }

    @Test
    void signalForUnknownWorkflow_shouldReachNothing() {
        assertTrue(engine.onSignal(InboundSignal.completion(
                "missing", "create", new Created("p-1"), "created:p-1")).isEmpty());
    }

    @Test
    void duplicateCompletion_shouldNotAdvanceTwice() {
        WorkflowInstance started = engine.start(definition, DemoContext.blank(), null);
        InboundSignal signal = InboundSignal.completion(
                started.id(),
                "create",
                new Created("p-1"),
                "created:p-1"
        );
        engine.onSignal(signal);
        engine.onSignal(signal);

        DemoContext context = new WorkflowPayloadCodec()
                .readTyped(store.findById(started.id()).orElseThrow().contextJson().orElseThrow(), DemoContext.class);
        assertEquals("p-1", context.productId());
        assertEquals("wait-view", store.findById(started.id()).orElseThrow().currentStep().orElseThrow());
    }

    @Test
    void failure_shouldPublishCompensationAndWaitForAck() {
        WorkflowInstance started = engine.start(definition, DemoContext.blank(), null);
        engine.onSignal(InboundSignal.completion(
                started.id(), "create", new Created("p-1"), "created:p-1"));
        published.clear();

        engine.onSignal(InboundSignal.failure(
                started.id(),
                "wait-view",
                new Failed(started.id(), "wait-view", "boom"),
                "fail:wait-view"
        ));

        WorkflowInstance compensating = store.findById(started.id()).orElseThrow();
        assertEquals(WorkflowStatus.COMPENSATING, compensating.status());
        assertEquals(1, published.size());
        assertInstanceOf(DeleteCreated.class, published.getFirst());
        assertTrue(terminals.isEmpty());

        engine.onSignal(InboundSignal.compensationAck(
                started.id(),
                new Deleted("p-1"),
                "deleted:p-1"
        ));

        WorkflowInstance compensated = store.findById(started.id()).orElseThrow();
        assertEquals(WorkflowStatus.COMPENSATED, compensated.status());
        assertEquals(1, terminals.size());
    }

    @Test
    void failureBeforeAnyWork_shouldMarkFailed() {
        WorkflowInstance started = engine.start(definition, DemoContext.blank(), null);
        published.clear();

        engine.onSignal(InboundSignal.failure(
                started.id(),
                "create",
                new Failed(started.id(), "create", "nope"),
                "fail:create"
        ));

        WorkflowInstance failed = store.findById(started.id()).orElseThrow();
        assertEquals(WorkflowStatus.FAILED, failed.status());
        assertTrue(published.isEmpty());
        assertEquals(1, terminals.size());
    }

    @Test
    void compensationAck_whenStepStillOwesCompensation_shouldStayCompensating() {
        WorkflowInstance started = engine.start(forgetful, DemoContext.blank(), null);
        engine.onSignal(InboundSignal.completion(
                started.id(), "create", new Created("p-1"), "created:p-1"));
        engine.onSignal(InboundSignal.failure(
                started.id(),
                "wait-view",
                new Failed(started.id(), "wait-view", "boom"),
                "fail:wait-view"
        ));

        engine.onSignal(InboundSignal.compensationAck(
                started.id(),
                new Deleted("p-1"),
                "deleted:p-1"
        ));

        assertEquals(WorkflowStatus.COMPENSATING, store.findById(started.id()).orElseThrow().status());
        assertTrue(terminals.isEmpty());
    }

    @Test
    void sweep_shouldCompensateTimedOutWaitingStep() {
        WorkflowInstance started = engine.start(definition, DemoContext.blank(), "timeout-1");
        engine.onSignal(InboundSignal.completion(
                started.id(), "create", new Created("p-1"), "created:p-1"));
        published.clear();
        WorkflowInstance waiting = store.findById(started.id()).orElseThrow();
        WorkflowInstance stale = WorkflowInstance.restore(
                waiting.id(),
                "demo",
                waiting.id(),
                "timeout-1",
                WorkflowStatus.WAITING_EXTERNAL,
                "wait-view",
                waiting.contextJson().orElseThrow(),
                waiting.checkpointJson().orElse("[]"),
                null,
                Instant.now().minus(Duration.ofHours(2)),
                Instant.now().minus(Duration.ofHours(1)),
                waiting.checkpoints(),
                waiting.version()
        );
        store.save(stale);

        engine.sweep();

        WorkflowInstance afterSweep = store.findById(started.id()).orElseThrow();
        assertEquals(WorkflowStatus.COMPENSATING, afterSweep.status());
        assertEquals(1, published.size());
        assertInstanceOf(DeleteCreated.class, published.getFirst());
    }

    private record DemoContext(String productId, boolean viewed, boolean finished, boolean deleted) {
        static DemoContext blank() {
            return new DemoContext(null, false, false, false);
        }

        DemoContext withProduct(String id) {
            return new DemoContext(id, viewed, finished, deleted);
        }

        DemoContext withViewed() {
            return new DemoContext(productId, true, finished, deleted);
        }

        DemoContext withFinished() {
            return new DemoContext(productId, viewed, true, deleted);
        }

        DemoContext withDeleted() {
            return new DemoContext(productId, viewed, finished, true);
        }
    }

    private static final class CreateStep implements StepDefinition<DemoContext> {
        @Override
        public String name() {
            return "create";
        }

        @Override
        public List<Event> onEnter(String workflowId, DemoContext context) {
            return List.of(new RequestCreated());
        }

        @Override
        public DemoContext onSignal(DemoContext context, InboundSignal signal) {
            if (signal.event() instanceof Created created) {
                return context.withProduct(created.productId());
            }
            return context;
        }

        @Override
        public boolean isComplete(DemoContext context) {
            return context.productId() != null;
        }

        @Override
        public Object checkpointOutput(DemoContext context) {
            return context.productId();
        }

        @Override
        public List<Event> compensate(String workflowId, DemoContext context) {
            if (context.productId() == null || context.deleted()) {
                return List.of();
            }
            return List.of(new DeleteCreated(context.productId()));
        }

        @Override
        public DemoContext onCompensationAck(DemoContext context, InboundSignal signal) {
            if (signal.event() instanceof Deleted) {
                return context.withDeleted();
            }
            return context;
        }

        @Override
        public boolean isCompensated(DemoContext context) {
            return context.productId() == null || context.deleted();
        }
    }

    /** Declares compensation but never reports progress, relying on the isCompensated default. */
    private static final class ForgetfulCompensationStep implements StepDefinition<DemoContext> {
        @Override
        public String name() {
            return "create";
        }

        @Override
        public List<Event> onEnter(String workflowId, DemoContext context) {
            return List.of(new RequestCreated());
        }

        @Override
        public DemoContext onSignal(DemoContext context, InboundSignal signal) {
            if (signal.event() instanceof Created created) {
                return context.withProduct(created.productId());
            }
            return context;
        }

        @Override
        public boolean isComplete(DemoContext context) {
            return context.productId() != null;
        }

        @Override
        public List<Event> compensate(String workflowId, DemoContext context) {
            if (context.productId() == null) {
                return List.of();
            }
            return List.of(new DeleteCreated(context.productId()));
        }
    }

    private static final class WaitViewStep implements StepDefinition<DemoContext> {
        @Override
        public String name() {
            return "wait-view";
        }

        @Override
        public List<CorrelationKey> correlationsOnEnter(DemoContext context) {
            return context.productId() == null
                    ? List.of()
                    : List.of(CorrelationKey.product(context.productId()));
        }

        @Override
        public DemoContext onSignal(DemoContext context, InboundSignal signal) {
            if (signal.event() instanceof Viewed viewed && viewed.productId().equals(context.productId())) {
                return context.withViewed();
            }
            return context;
        }

        @Override
        public boolean isComplete(DemoContext context) {
            return context.viewed();
        }
    }

    private static final class FinishStep implements StepDefinition<DemoContext> {
        @Override
        public String name() {
            return "finish";
        }

        @Override
        public DemoContext onSignal(DemoContext context, InboundSignal signal) {
            return context.withFinished();
        }

        @Override
        public boolean isComplete(DemoContext context) {
            return true;
        }
    }

    private record RequestCreated() implements Event {
    }

    private record Created(String productId) implements Event {
    }

    private record Viewed(String productId, String sku) implements Event {
    }

    private record DeleteCreated(String productId) implements Event {
    }

    private record Deleted(String productId) implements Event {
    }

    private record Failed(String workflowId, String step, String message) implements Event, WorkflowStepFailure {
    }

    private static final class SequentialIdGenerator implements IdGenerator {
        private int counter;

        @Override
        public Id generateId() {
            return new CommonId("wf-" + (++counter));
        }

        @Override
        public Id convertIdFrom(String id) {
            return new CommonId(id);
        }
    }
}
