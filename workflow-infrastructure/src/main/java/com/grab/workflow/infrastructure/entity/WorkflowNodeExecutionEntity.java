package com.grab.workflow.infrastructure.entity;

import com.grab.framework.workflow.NodeExecutionStatus;
import com.grab.framework.workflow.WorkflowPhase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "workflow_node_execution")
@IdClass(WorkflowNodeExecutionEntity.Pk.class)
public class WorkflowNodeExecutionEntity {

    @Id
    @Column(name = "execution_id", nullable = false, length = 64)
    private String executionId;

    @Id
    @Column(name = "node_id", nullable = false, length = 128)
    private String nodeId;

    @Column(name = "branch_id", nullable = false, length = 128)
    private String branchId;

    @Column(name = "logical_operation_id", length = 256)
    private String logicalOperationId;

    @Column(name = "attempt_id", length = 256)
    private String attemptId;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase", nullable = false, length = 32)
    private WorkflowPhase phase;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private NodeExecutionStatus status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "output_json", columnDefinition = "TEXT")
    private String outputJson;

    @Column(name = "input_json", columnDefinition = "TEXT")
    private String inputJson;

    @Column(name = "accepted_outcome_json", columnDefinition = "TEXT")
    private String acceptedOutcomeJson;

    @Column(name = "node_generation", nullable = false)
    private long nodeGeneration;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "deadline_at")
    private Instant deadlineAt;

    @Column(name = "retry_at")
    private Instant retryAt;

    protected WorkflowNodeExecutionEntity() {
    }

    public static WorkflowNodeExecutionEntity empty() {
        return new WorkflowNodeExecutionEntity();
    }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getLogicalOperationId() { return logicalOperationId; }
    public void setLogicalOperationId(String logicalOperationId) { this.logicalOperationId = logicalOperationId; }
    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }
    public WorkflowPhase getPhase() { return phase; }
    public void setPhase(WorkflowPhase phase) { this.phase = phase; }
    public NodeExecutionStatus getStatus() { return status; }
    public void setStatus(NodeExecutionStatus status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public String getOutputJson() { return outputJson; }
    public void setOutputJson(String outputJson) { this.outputJson = outputJson; }
    public String getInputJson() { return inputJson; }
    public void setInputJson(String inputJson) { this.inputJson = inputJson; }
    public String getAcceptedOutcomeJson() { return acceptedOutcomeJson; }
    public void setAcceptedOutcomeJson(String acceptedOutcomeJson) { this.acceptedOutcomeJson = acceptedOutcomeJson; }
    public long getNodeGeneration() { return nodeGeneration; }
    public void setNodeGeneration(long nodeGeneration) { this.nodeGeneration = nodeGeneration; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(Instant deadlineAt) { this.deadlineAt = deadlineAt; }
    public Instant getRetryAt() { return retryAt; }
    public void setRetryAt(Instant retryAt) { this.retryAt = retryAt; }

    public static final class Pk implements Serializable {
        private String executionId;
        private String nodeId;

        public Pk() { }
        public Pk(String executionId, String nodeId) {
            this.executionId = executionId;
            this.nodeId = nodeId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Pk pk)) return false;
            return Objects.equals(executionId, pk.executionId) && Objects.equals(nodeId, pk.nodeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(executionId, nodeId);
        }
    }
}
