package com.grab.framework.workflow;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class WorkflowInstance {

    @Getter
    private final String id;
    @Getter
    private final String workflowName;
    @Getter
    private final String correlationId;
    private final String idempotencyKey;
    @Getter
    private WorkflowStatus status;
    private String currentStep;
    private String contextJson;
    private String checkpointJson;
    private String errorMessage;
    @Getter
    private final Instant createdAt;
    @Getter
    private Instant updatedAt;
    private final List<WorkflowCheckpoint> checkpoints = new ArrayList<>();

    private WorkflowInstance(
            String id,
            String workflowName,
            String correlationId,
            String idempotencyKey,
            WorkflowStatus status,
            String currentStep,
            String contextJson,
            String checkpointJson,
            String errorMessage,
            Instant createdAt,
            Instant updatedAt,
            List<WorkflowCheckpoint> checkpoints
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.workflowName = Objects.requireNonNull(workflowName, "workflowName");
        this.correlationId = Objects.requireNonNull(correlationId, "correlationId");
        this.idempotencyKey = idempotencyKey;
        this.status = Objects.requireNonNull(status, "status");
        this.currentStep = currentStep;
        this.contextJson = contextJson;
        this.checkpointJson = checkpointJson;
        this.errorMessage = errorMessage;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        if (checkpoints != null) {
            this.checkpoints.addAll(checkpoints);
        }
    }

    public static WorkflowInstance start(
            String id,
            String workflowName,
            String correlationId,
            String idempotencyKey
    ) {
        Instant now = Instant.now();
        return new WorkflowInstance(
                id,
                workflowName,
                correlationId,
                idempotencyKey,
                WorkflowStatus.RUNNING,
                null,
                null,
                null,
                null,
                now,
                now,
                List.of()
        );
    }

    public static WorkflowInstance restore(
            String id,
            String workflowName,
            String correlationId,
            String idempotencyKey,
            WorkflowStatus status,
            String currentStep,
            String contextJson,
            String checkpointJson,
            String errorMessage,
            Instant createdAt,
            Instant updatedAt,
            List<WorkflowCheckpoint> checkpoints
    ) {
        return new WorkflowInstance(
                id,
                workflowName,
                correlationId,
                idempotencyKey,
                status,
                currentStep,
                contextJson,
                checkpointJson,
                errorMessage,
                createdAt,
                updatedAt,
                checkpoints
        );
    }

    public void recordCheckpoint(String stepName, Object output, String contextJson, String checkpointJson) {
        checkpoints.add(new WorkflowCheckpoint(stepName, output));
        this.currentStep = stepName;
        this.contextJson = contextJson;
        this.checkpointJson = checkpointJson;
        touch();
    }

    public void markCompleted(String contextJson, String checkpointJson) {
        this.status = WorkflowStatus.COMPLETED;
        this.contextJson = contextJson;
        this.checkpointJson = checkpointJson;
        this.errorMessage = null;
        touch();
    }

    public void markFailed(String failedStep, String errorMessage, String contextJson, String checkpointJson) {
        this.status = WorkflowStatus.FAILED;
        this.currentStep = failedStep;
        this.errorMessage = errorMessage;
        this.contextJson = contextJson;
        this.checkpointJson = checkpointJson;
        touch();
    }

    public void beginCompensation(String failedStep, String errorMessage) {
        this.status = WorkflowStatus.COMPENSATING;
        this.currentStep = failedStep;
        this.errorMessage = errorMessage;
        touch();
    }

    public void markCompensated(String contextJson, String checkpointJson) {
        this.status = WorkflowStatus.COMPENSATED;
        this.contextJson = contextJson;
        this.checkpointJson = checkpointJson;
        touch();
    }

    public void markWaitingExternal(String currentStep, String contextJson, String checkpointJson) {
        this.status = WorkflowStatus.WAITING_EXTERNAL;
        this.currentStep = currentStep;
        this.contextJson = contextJson;
        this.checkpointJson = checkpointJson;
        touch();
    }

    public void markRunning() {
        this.status = WorkflowStatus.RUNNING;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public String id() {
        return id;
    }

    public String workflowName() {
        return workflowName;
    }

    public String correlationId() {
        return correlationId;
    }

    public Optional<String> idempotencyKey() {
        return Optional.ofNullable(idempotencyKey);
    }

    public Optional<String> getIdempotencyKey() {
        return idempotencyKey();
    }

    public WorkflowStatus status() {
        return status;
    }

    public Optional<String> currentStep() {
        return Optional.ofNullable(currentStep);
    }

    public Optional<String> getCurrentStep() {
        return currentStep();
    }

    public Optional<String> contextJson() {
        return Optional.ofNullable(contextJson);
    }

    public Optional<String> getContextJson() {
        return contextJson();
    }

    public Optional<String> checkpointJson() {
        return Optional.ofNullable(checkpointJson);
    }

    public Optional<String> getCheckpointJson() {
        return checkpointJson();
    }

    public Optional<String> errorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public Optional<String> getErrorMessage() {
        return errorMessage();
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public List<WorkflowCheckpoint> checkpoints() {
        return Collections.unmodifiableList(checkpoints);
    }
}
