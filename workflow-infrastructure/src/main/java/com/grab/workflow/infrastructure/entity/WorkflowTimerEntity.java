package com.grab.workflow.infrastructure.entity;

import com.grab.framework.workflow.WorkflowPhase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "workflow_timer", indexes = {
        @Index(name = "idx_workflow_timer_due", columnList = "status, due_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_workflow_timer_generation", columnNames = {"execution_id", "node_id", "node_generation", "purpose", "phase"})
})
public class WorkflowTimerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "execution_id", nullable = false, length = 64)
    private String executionId;

    @Column(name = "node_id", nullable = false, length = 128)
    private String nodeId;

    @Column(name = "node_generation", nullable = false)
    private long nodeGeneration;

    @Column(name = "logical_operation_id", nullable = false, length = 256)
    private String logicalOperationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase", nullable = false, length = 32)
    private WorkflowPhase phase;

    @Column(name = "purpose", nullable = false, length = 64)
    private String purpose;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private WorkflowTimerStatus status;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "claim_token", length = 128)
    private String claimToken;

    @Column(name = "lease_expires_at")
    private Instant leaseExpiresAt;

    protected WorkflowTimerEntity() {
    }

    public static WorkflowTimerEntity scheduled() {
        return new WorkflowTimerEntity();
    }

    public Long getId() { return id; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public long getNodeGeneration() { return nodeGeneration; }
    public void setNodeGeneration(long nodeGeneration) { this.nodeGeneration = nodeGeneration; }
    public String getLogicalOperationId() { return logicalOperationId; }
    public void setLogicalOperationId(String logicalOperationId) { this.logicalOperationId = logicalOperationId; }
    public WorkflowPhase getPhase() { return phase; }
    public void setPhase(WorkflowPhase phase) { this.phase = phase; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public Instant getDueAt() { return dueAt; }
    public void setDueAt(Instant dueAt) { this.dueAt = dueAt; }
    public WorkflowTimerStatus getStatus() { return status; }
    public void setStatus(WorkflowTimerStatus status) { this.status = status; }
    public Instant getClaimedAt() { return claimedAt; }
    public void setClaimedAt(Instant claimedAt) { this.claimedAt = claimedAt; }
    public String getClaimToken() { return claimToken; }
    public void setClaimToken(String claimToken) { this.claimToken = claimToken; }
    public Instant getLeaseExpiresAt() { return leaseExpiresAt; }
    public void setLeaseExpiresAt(Instant leaseExpiresAt) { this.leaseExpiresAt = leaseExpiresAt; }
}
