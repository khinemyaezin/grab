package com.grab.framework.workflow.impl;

import com.grab.framework.workflow.WorkflowCheckpoint;
import com.grab.framework.workflow.WorkflowContext;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowRunner;
import com.grab.framework.workflow.WorkflowResult;
import com.grab.framework.workflow.WorkflowRunRequest;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.WorkflowStep;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class DefaultWorkflowRunner implements WorkflowRunner {

    private static final Set<WorkflowStatus> RESUMABLE = Set.of(
            WorkflowStatus.RUNNING,
            WorkflowStatus.WAITING_EXTERNAL
    );

    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;

    public DefaultWorkflowRunner(WorkflowStore workflowStore) {
        this(workflowStore, new WorkflowPayloadCodec());
    }

    public DefaultWorkflowRunner(WorkflowStore workflowStore, WorkflowPayloadCodec payloadCodec) {
        this.workflowStore = Objects.requireNonNull(workflowStore, "workflowStore");
        this.payloadCodec = Objects.requireNonNull(payloadCodec, "payloadCodec");
    }

    @Override
    public WorkflowResult run(WorkflowDefinition definition, WorkflowContext context, WorkflowRunRequest request) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(request, "request");

        Optional<WorkflowInstance> byId = workflowStore.findById(request.workflowId());
        if (byId.isPresent()) {
            return continueOrReplay(definition, context, byId.get());
        }

        Optional<String> idempotencyKey = request.idempotencyKey();
        if (idempotencyKey.isPresent()) {
            Optional<WorkflowInstance> byIdempotency = workflowStore.findByIdempotencyKey(
                    definition.name(),
                    idempotencyKey.get()
            );
            if (byIdempotency.isPresent()) {
                return continueOrReplay(definition, context, byIdempotency.get());
            }
        }

        WorkflowInstance instance = WorkflowInstance.start(
                request.workflowId(),
                definition.name(),
                context.correlationId(),
                idempotencyKey.orElse(null)
        );
        workflowStore.save(instance);

        return executeFrom(definition, context, instance, List.of(), 0);
    }

    @Override
    public WorkflowResult resume(WorkflowDefinition definition, String workflowId) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(workflowId, "workflowId");
        if (workflowId.isBlank()) {
            throw new IllegalArgumentException("workflowId must not be blank");
        }

        WorkflowInstance instance = workflowStore.findById(workflowId)
                .orElseThrow(() -> new IllegalStateException("Workflow not found: " + workflowId));

        WorkflowContext context = new WorkflowContext(instance.correlationId());
        return continueOrReplay(definition, context, instance);
    }

    private WorkflowResult continueOrReplay(
            WorkflowDefinition definition,
            WorkflowContext context,
            WorkflowInstance existing
    ) {
        if (!existing.workflowName().equals(definition.name())) {
            throw new IllegalStateException(
                    "Workflow name mismatch: stored=" + existing.workflowName()
                            + ", definition=" + definition.name()
            );
        }

        WorkflowStatus status = existing.status();
        if (status == WorkflowStatus.COMPLETED) {
            existing.contextJson().ifPresent(json -> payloadCodec.applyContext(context, json));
            return WorkflowResult.success(existing.workflowName(), existing.id(), context);
        }

        if (!RESUMABLE.contains(status)) {
            throw new IllegalStateException(
                    "Workflow cannot be resumed with status " + status
                            + " (workflowId=" + existing.id() + ")"
            );
        }

        return resumeFromCheckpoint(definition, context, existing);
    }

    private WorkflowResult resumeFromCheckpoint(
            WorkflowDefinition definition,
            WorkflowContext context,
            WorkflowInstance instance
    ) {
        instance.contextJson().ifPresent(json -> payloadCodec.applyContext(context, json));
        List<CompletedStep> completedSteps = rebuildCompletedSteps(definition, instance, context);
        int startIndex = completedSteps.size();

        if (instance.status() == WorkflowStatus.WAITING_EXTERNAL) {
            instance.markRunning();
            workflowStore.save(instance);
        }

        return executeFrom(definition, context, instance, completedSteps, startIndex);
    }

    private List<CompletedStep> rebuildCompletedSteps(
            WorkflowDefinition definition,
            WorkflowInstance instance,
            WorkflowContext context
    ) {
        List<WorkflowCheckpoint> checkpoints = instance.checkpoints();
        List<WorkflowStep> steps = definition.steps();
        if (checkpoints.size() > steps.size()) {
            throw new IllegalStateException(
                    "Checkpoint count " + checkpoints.size()
                            + " exceeds definition step count " + steps.size()
            );
        }

        List<CompletedStep> completedSteps = new ArrayList<>(checkpoints.size());
        for (int index = 0; index < checkpoints.size(); index++) {
            WorkflowCheckpoint checkpoint = checkpoints.get(index);
            WorkflowStep step = steps.get(index);
            if (!step.name().equals(checkpoint.stepName())) {
                throw new IllegalStateException(
                        "Checkpoint mismatch at index " + index
                                + ": expected step '" + step.name()
                                + "', stored '" + checkpoint.stepName() + "'"
                );
            }
            completedSteps.add(new CompletedStep(step, checkpoint.output()));
            context.put(step.name(), checkpoint.output());
        }
        return completedSteps;
    }

    private WorkflowResult executeFrom(
            WorkflowDefinition definition,
            WorkflowContext context,
            WorkflowInstance instance,
            List<CompletedStep> alreadyCompleted,
            int startIndex
    ) {
        List<CompletedStep> completedSteps = new ArrayList<>(alreadyCompleted);
        List<WorkflowStep> steps = definition.steps();

        for (int index = startIndex; index < steps.size(); index++) {
            WorkflowStep step = steps.get(index);
            try {
                Object output = step.execute(context);
                completedSteps.add(new CompletedStep(step, output));
                context.put(step.name(), output);

                String contextJson = payloadCodec.writeContext(context);
                List<WorkflowCheckpoint> checkpoints = completedSteps.stream()
                        .map(completed -> new WorkflowCheckpoint(completed.step().name(), completed.output()))
                        .toList();
                String checkpointJson = payloadCodec.writeCheckpoints(checkpoints);
                instance.recordCheckpoint(step.name(), output, contextJson, checkpointJson);
                workflowStore.save(instance);
            } catch (RuntimeException exception) {
                return failAndCompensate(definition, context, instance, completedSteps, step.name(), exception);
            }
        }

        String contextJson = payloadCodec.writeContext(context);
        String checkpointJson = payloadCodec.writeCheckpoints(instance.checkpoints());
        instance.markCompleted(contextJson, checkpointJson);
        workflowStore.save(instance);

        return WorkflowResult.success(definition.name(), instance.id(), context);
    }

    private WorkflowResult failAndCompensate(
            WorkflowDefinition definition,
            WorkflowContext context,
            WorkflowInstance instance,
            List<CompletedStep> completedSteps,
            String failedStep,
            RuntimeException exception
    ) {
        instance.beginCompensation(failedStep, exception.getMessage());
        workflowStore.save(instance);

        boolean compensationCompleted = compensateCompletedSteps(completedSteps, context);
        String contextJson = payloadCodec.writeContext(context);
        String checkpointJson = payloadCodec.writeCheckpoints(instance.checkpoints());

        if (compensationCompleted) {
            instance.markCompensated(contextJson, checkpointJson);
        } else {
            instance.markFailed(failedStep, exception.getMessage(), contextJson, checkpointJson);
        }
        workflowStore.save(instance);

        return WorkflowResult.failed(
                definition.name(),
                instance.id(),
                context,
                failedStep,
                exception,
                compensationCompleted
        );
    }

    private boolean compensateCompletedSteps(List<CompletedStep> completedSteps, WorkflowContext context) {
        boolean compensationCompleted = true;
        for (int index = completedSteps.size() - 1; index >= 0; index--) {
            CompletedStep completedStep = completedSteps.get(index);
            try {
                completedStep.step().compensate(context, completedStep.output());
            } catch (RuntimeException compensationException) {
                compensationCompleted = false;
            }
        }
        return compensationCompleted;
    }

    private record CompletedStep(WorkflowStep step, Object output) {
    }
}
