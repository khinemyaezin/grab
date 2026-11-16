package com.grab.workflow.infrastructure.entity;

import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowSignalOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import java.time.Instant;

@Entity
@Table(name = "workflow_signal_receipt", indexes = {
        @Index(name = "idx_workflow_signal_outcome_fingerprint", columnList = "execution_id, logical_operation_id, outcome_fingerprint, fingerprint_version, transport_message_id")
})
public class WorkflowSignalReceiptEntity {

    @Id
    @Column(name = "transport_message_id", nullable = false, length = 256)
    private String transportMessageId;

    @Column(name = "execution_id", nullable = false, length = 64)
    private String executionId;

    @Column(name = "node_id", nullable = false, length = 128)
    private String nodeId;

    @Column(name = "logical_operation_id", nullable = false, length = 256)
    private String logicalOperationId;

    @Column(name = "attempt_id", nullable = false, length = 256)
    private String attemptId;

    @Column(name = "attempt", nullable = false)
    private int attempt;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase", nullable = false, length = 32)
    private WorkflowPhase phase;

    @Column(name = "payload_fingerprint", nullable = false, length = 128)
    private String payloadFingerprint;

    @Column(name = "outcome_fingerprint", length = 128)
    private String outcomeFingerprint;

    public String getOutcomeFingerprint() {
        return outcomeFingerprint;
    }

    public void setOutcomeFingerprint(String outcomeFingerprint) {
        this.outcomeFingerprint = outcomeFingerprint;
    }

    @Column(name = "fingerprint_version", nullable = false)
    private int fingerprintVersion;

    @Column(name = "payload_version", nullable = false)
    private int payloadVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition", nullable = false, length = 32)
    private WorkflowSignalDisposition disposition;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 32)
    private WorkflowSignalOutcome outcome;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected WorkflowSignalReceiptEntity() {
    }

    public static WorkflowSignalReceiptEntity empty() {
        return new WorkflowSignalReceiptEntity();
    }

    public String getTransportMessageId() { return transportMessageId; }
    public void setTransportMessageId(String transportMessageId) { this.transportMessageId = transportMessageId; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getLogicalOperationId() { return logicalOperationId; }
    public void setLogicalOperationId(String logicalOperationId) { this.logicalOperationId = logicalOperationId; }
    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }
    public int getAttempt() { return attempt; }
    public void setAttempt(int attempt) { this.attempt = attempt; }
    public WorkflowPhase getPhase() { return phase; }
    public void setPhase(WorkflowPhase phase) { this.phase = phase; }
    public String getPayloadFingerprint() { return payloadFingerprint; }
    public void setPayloadFingerprint(String payloadFingerprint) { this.payloadFingerprint = payloadFingerprint; }
    public int getFingerprintVersion() { return fingerprintVersion; }
    public void setFingerprintVersion(int fingerprintVersion) { this.fingerprintVersion = fingerprintVersion; }
    public int getPayloadVersion() { return payloadVersion; }
    public void setPayloadVersion(int payloadVersion) { this.payloadVersion = payloadVersion; }
    public WorkflowSignalDisposition getDisposition() { return disposition; }
    public void setDisposition(WorkflowSignalDisposition disposition) { this.disposition = disposition; }
    public WorkflowSignalOutcome getOutcome() { return outcome; }
    public void setOutcome(WorkflowSignalOutcome outcome) { this.outcome = outcome; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
