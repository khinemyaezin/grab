package com.grab.framework.workflow.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.workflow.CorrelationKey;
import com.grab.framework.workflow.InboundSignal;
import com.grab.framework.workflow.ProcessDefinition;
import com.grab.framework.workflow.SignalType;
import com.grab.framework.workflow.StepDefinition;
import com.grab.framework.workflow.WorkflowCheckpoint;
import com.grab.framework.workflow.WorkflowDefinitionRegistry;
import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowLifecycleListener;
import com.grab.framework.workflow.WorkflowOptimisticLockException;
import com.grab.framework.workflow.WorkflowSignalPublisher;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.WorkflowStepFailure;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class EventDrivenWorkflowEngine implements WorkflowEngine {

    private static final Set<WorkflowStatus> TERMINAL = Set.of(
            WorkflowStatus.COMPLETED,
            WorkflowStatus.COMPENSATED,
            WorkflowStatus.FAILED
    );
    private static final Set<WorkflowStatus> SWEEPABLE = Set.of(
            WorkflowStatus.WAITING_EXTERNAL,
            WorkflowStatus.COMPENSATING
    );

    private static final int SWEEP_BATCH = 50;

    private final WorkflowStore workflowStore;
    private final WorkflowDefinitionRegistry definitionRegistry;
    private final WorkflowPayloadCodec payloadCodec;
    private final WorkflowSignalPublisher signalPublisher;
    private final IdGenerator idGenerator;
    private final WorkflowLifecycleListener lifecycleListener;

    public EventDrivenWorkflowEngine(
            WorkflowStore workflowStore,
            WorkflowDefinitionRegistry definitionRegistry,
            WorkflowPayloadCodec payloadCodec,
            WorkflowSignalPublisher signalPublisher,
            IdGenerator idGenerator,
            WorkflowLifecycleListener lifecycleListener
    ) {
        this.workflowStore = Objects.requireNonNull(workflowStore, "workflowStore");
        this.definitionRegistry = Objects.requireNonNull(definitionRegistry, "definitionRegistry");
        this.payloadCodec = Objects.requireNonNull(payloadCodec, "payloadCodec");
        this.signalPublisher = Objects.requireNonNull(signalPublisher, "signalPublisher");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.lifecycleListener = lifecycleListener == null
                ? new WorkflowLifecycleListener() {
                }
                : lifecycleListener;
    }

    @Override
    public <C> WorkflowInstance start(ProcessDefinition<C> definition, C input, String idempotencyKey) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(input, "input");

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<WorkflowInstance> existing = workflowStore.findByIdempotencyKey(
                    definition.name(),
                    idempotencyKey
            );
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        String workflowId = idGenerator.generateId().getValue();
        WorkflowInstance instance = WorkflowInstance.start(
                workflowId,
                definition.name(),
                workflowId,
                idempotencyKey
        );
        return enterFrom(definition, instance, input, 0);
    }

    @Override
    public List<WorkflowInstance> onSignal(InboundSignal signal) {
        Objects.requireNonNull(signal, "signal");
        List<WorkflowInstance> applied = new ArrayList<>();
        for (WorkflowInstance instance : resolveInstances(signal)) {
            definitionRegistry.find(instance.workflowName())
                    .map(definition -> handleSignal(definition, instance, signal))
                    .ifPresent(applied::add);
        }
        return List.copyOf(applied);
    }

    @Override
    public Optional<WorkflowInstance> resume(String workflowId) {
        Objects.requireNonNull(workflowId, "workflowId");
        return workflowStore.findById(workflowId)
                .flatMap(instance -> definitionRegistry.find(instance.workflowName())
                        .map(definition -> resumeCaptured(definition, instance)));
    }

    @Override
    public void sweep() {
        Instant now = Instant.now();
        for (ProcessDefinition<?> definition : definitionRegistry.all()) {
            sweepCaptured(definition, now);
        }
    }

    private <C> WorkflowInstance resumeCaptured(ProcessDefinition<C> definition, WorkflowInstance instance) {
        C context = readContext(definition, instance);
        if (instance.status() == WorkflowStatus.WAITING_EXTERNAL) {
            StepDefinition<C> step = currentStep(definition, instance);
            if (step != null) {
                signalPublisher.publish(instance.id(), step.onEnter(instance.id(), context));
            }
            return instance;
        }
        if (instance.status() == WorkflowStatus.COMPENSATING) {
            signalPublisher.publish(instance.id(), collectCompensationEvents(definition, instance.id(), context));
        }
        return instance;
    }

    private <C> void sweepCaptured(ProcessDefinition<C> definition, Instant now) {
        Instant idleSince = now.minus(shortestTimeout(definition));
        List<WorkflowInstance> candidates = workflowStore.findResumable(
                definition.name(),
                SWEEPABLE,
                idleSince,
                SWEEP_BATCH
        );
        for (WorkflowInstance instance : candidates) {
            Duration timeout = timeoutFor(definition, instance);
            if (!instance.updatedAt().plus(timeout).isBefore(now)) {
                continue;
            }
            if (instance.status() == WorkflowStatus.WAITING_EXTERNAL) {
                String stepName = instance.currentStep().orElse("unknown");
                beginCompensation(
                        definition,
                        instance,
                        stepName,
                        "Timed out waiting on step " + stepName
                );
            } else if (instance.status() == WorkflowStatus.COMPENSATING) {
                C context = readContext(definition, instance);
                String contextJson = payloadCodec.writeTyped(context);
                String checkpointJson = instance.checkpointJson()
                        .orElseGet(() -> payloadCodec.writeCheckpoints(instance.checkpoints()));
                instance.markFailed(
                        instance.currentStep().orElse("unknown"),
                        "Compensation timed out",
                        contextJson,
                        checkpointJson
                );
                workflowStore.save(instance);
                workflowStore.removeCorrelationsForWorkflow(instance.id());
                lifecycleListener.onTerminal(instance, context);
            }
        }
    }

    private <C> WorkflowInstance handleSignal(
            ProcessDefinition<C> definition,
            WorkflowInstance instance,
            InboundSignal signal
    ) {
        if (TERMINAL.contains(instance.status())) {
            return instance;
        }
        if (signal.type() == SignalType.FAILURE) {
            WorkflowStepFailure failure = failureOf(signal);
            String failedStep = failure != null
                    ? failure.step()
                    : signal.stepHintOptional().orElse(instance.currentStep().orElse("unknown"));
            String message = failure != null ? failure.message() : "Workflow step failed";
            return beginCompensation(definition, instance, failedStep, message);
        }
        if (instance.status() == WorkflowStatus.COMPENSATING) {
            if (signal.type() != SignalType.COMPENSATION_ACK) {
                return instance;
            }
            return onCompensationAck(definition, instance, signal);
        }
        if (instance.status() != WorkflowStatus.WAITING_EXTERNAL) {
            return instance;
        }

        StepDefinition<C> step = currentStep(definition, instance);
        if (step == null) {
            return instance;
        }
        if (signal.stepHintOptional().isPresent() && !step.name().equals(signal.stepHintOptional().get())) {
            return instance;
        }
        if (!workflowStore.tryRecordSignal(instance.id(), step.name(), signal.dedupKey())) {
            return instance;
        }

        C context = readContext(definition, instance);
        C updated = step.onSignal(context, signal);
        if (updated == null) {
            updated = context;
        }
        if (!step.isComplete(updated)) {
            String contextJson = payloadCodec.writeTyped(updated);
            instance.markWaitingExternal(
                    step.name(),
                    contextJson,
                    instance.checkpointJson().orElse(null)
            );
            return workflowStore.save(instance);
        }

        appendCheckpoint(instance, step, updated);
        int nextIndex = definition.indexOf(step.name()) + 1;
        return enterFrom(definition, instance, updated, nextIndex);
    }

    private <C> WorkflowInstance enterFrom(
            ProcessDefinition<C> definition,
            WorkflowInstance instance,
            C context,
            int startIndex
    ) {
        List<StepDefinition<C>> steps = definition.steps();
        C current = context;
        for (int index = startIndex; index < steps.size(); index++) {
            StepDefinition<C> step = steps.get(index);
            if (step.isComplete(current)) {
                appendCheckpoint(instance, step, current);
                continue;
            }
            List<Event> events;
            try {
                events = step.onEnter(instance.id(), current);
            } catch (RuntimeException exception) {
                return beginCompensation(definition, instance, step.name(), exception.getMessage());
            }
            String contextJson = payloadCodec.writeTyped(current);
            String checkpointJson = instance.checkpointJson()
                    .orElseGet(() -> payloadCodec.writeCheckpoints(instance.checkpoints()));
            instance.markWaitingExternal(step.name(), contextJson, checkpointJson);
            workflowStore.save(instance);
            registerCorrelations(definition, instance, step, current);
            signalPublisher.publish(instance.id(), events);
            return instance;
        }

        String contextJson = payloadCodec.writeTyped(current);
        String checkpointJson = instance.checkpointJson()
                .orElseGet(() -> payloadCodec.writeCheckpoints(instance.checkpoints()));
        instance.markCompleted(contextJson, checkpointJson);
        workflowStore.save(instance);
        workflowStore.removeCorrelationsForWorkflow(instance.id());
        lifecycleListener.onTerminal(instance, current);
        return instance;
    }

    private <C> WorkflowInstance beginCompensation(
            ProcessDefinition<C> definition,
            WorkflowInstance instance,
            String failedStep,
            String message
    ) {
        if (TERMINAL.contains(instance.status()) || instance.status() == WorkflowStatus.COMPENSATING) {
            return instance;
        }
        C context = readContext(definition, instance);
        instance.beginCompensation(failedStep, message);
        workflowStore.save(instance);

        List<Event> events = collectCompensationEvents(definition, instance.id(), context);
        String contextJson = payloadCodec.writeTyped(context);
        String checkpointJson = instance.checkpointJson()
                .orElseGet(() -> payloadCodec.writeCheckpoints(instance.checkpoints()));

        if (events.isEmpty()) {
            instance.markFailed(failedStep, message, contextJson, checkpointJson);
            workflowStore.save(instance);
            workflowStore.removeCorrelationsForWorkflow(instance.id());
            lifecycleListener.onTerminal(instance, context);
            return instance;
        }

        instance.updatePayload(contextJson, checkpointJson);
        workflowStore.save(instance);
        signalPublisher.publish(instance.id(), events);
        return instance;
    }

    private <C> WorkflowInstance onCompensationAck(
            ProcessDefinition<C> definition,
            WorkflowInstance instance,
            InboundSignal signal
    ) {
        String stepName = instance.currentStep().orElse("compensate");
        if (!workflowStore.tryRecordSignal(instance.id(), stepName, signal.dedupKey())) {
            return instance;
        }
        C context = readContext(definition, instance);
        C updated = context;
        for (StepDefinition<C> step : definition.steps()) {
            updated = step.onCompensationAck(updated, signal);
        }
        C latest = updated;
        String contextJson = payloadCodec.writeTyped(latest);
        String checkpointJson = instance.checkpointJson()
                .orElseGet(() -> payloadCodec.writeCheckpoints(instance.checkpoints()));
        if (!isFullyCompensated(definition, instance.id(), latest)) {
            instance.updatePayload(contextJson, checkpointJson);
            return workflowStore.save(instance);
        }
        instance.markCompensated(contextJson, checkpointJson);
        workflowStore.save(instance);
        workflowStore.removeCorrelationsForWorkflow(instance.id());
        lifecycleListener.onTerminal(instance, latest);
        return instance;
    }

    private <C> boolean isFullyCompensated(ProcessDefinition<C> definition, String workflowId, C context) {
        return definition.steps().stream()
                .allMatch(step -> step.isCompensated(context) && step.compensate(workflowId, context).isEmpty());
    }

    private <C> List<Event> collectCompensationEvents(
            ProcessDefinition<C> definition,
            String workflowId,
            C context
    ) {
        List<Event> events = new ArrayList<>();
        List<StepDefinition<C>> steps = definition.steps();
        for (int index = steps.size() - 1; index >= 0; index--) {
            events.addAll(steps.get(index).compensate(workflowId, context));
        }
        return events;
    }

    private <C> void registerCorrelations(
            ProcessDefinition<C> definition,
            WorkflowInstance instance,
            StepDefinition<C> step,
            C context
    ) {
        for (CorrelationKey key : step.correlationsOnEnter(context)) {
            workflowStore.putCorrelation(definition.name(), key.value(), instance.id(), step.name());
        }
    }

    private <C> void appendCheckpoint(WorkflowInstance instance, StepDefinition<C> step, C context) {
        Object output = step.checkpointOutput(context);
        List<WorkflowCheckpoint> checkpoints = new ArrayList<>(instance.checkpoints());
        checkpoints.add(new WorkflowCheckpoint(step.name(), output));
        String checkpointJson = payloadCodec.writeCheckpoints(checkpoints);
        String contextJson = payloadCodec.writeTyped(context);
        instance.recordCheckpoint(step.name(), output, contextJson, checkpointJson);
    }

    private List<WorkflowInstance> resolveInstances(InboundSignal signal) {
        Optional<String> workflowId = signal.workflowIdOptional();
        if (workflowId.isPresent()) {
            return workflowStore.findById(workflowId.get()).map(List::of).orElseGet(List::of);
        }
        return signal.correlationKeyOptional()
                .map(key -> workflowStore.findAllByCorrelationKey(key.value()))
                .orElseGet(List::of);
    }

    private <C> C readContext(ProcessDefinition<C> definition, WorkflowInstance instance) {
        return payloadCodec.readTyped(instance.contextJson().orElse(null), definition.contextType());
    }

    private <C> StepDefinition<C> currentStep(ProcessDefinition<C> definition, WorkflowInstance instance) {
        return instance.currentStep().flatMap(definition::step).orElse(null);
    }

    private <C> Duration timeoutFor(ProcessDefinition<C> definition, WorkflowInstance instance) {
        StepDefinition<C> step = currentStep(definition, instance);
        return step == null ? StepDefinition.DEFAULT_TIMEOUT : step.timeout();
    }

    private <C> Duration shortestTimeout(ProcessDefinition<C> definition) {
        return definition.steps().stream()
                .map(StepDefinition::timeout)
                .min(Duration::compareTo)
                .orElse(StepDefinition.DEFAULT_TIMEOUT);
    }

    private static WorkflowStepFailure failureOf(InboundSignal signal) {
        if (signal.event() instanceof WorkflowStepFailure failure) {
            return failure;
        }
        return null;
    }
}
