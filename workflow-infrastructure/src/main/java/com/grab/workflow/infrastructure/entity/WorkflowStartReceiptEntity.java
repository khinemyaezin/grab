package com.grab.workflow.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "workflow_start_receipt")
@IdClass(WorkflowStartReceiptEntity.Pk.class)
public class WorkflowStartReceiptEntity {

    @Id
    @Column(name = "caller_scope", nullable = false, length = 128)
    private String callerScope;

    @Id
    @Column(name = "idempotency_key", nullable = false, length = 256)
    private String idempotencyKey;

    @Column(name = "workflow_name", nullable = false, length = 128)
    private String workflowName;

    @Column(name = "definition_version", nullable = false)
    private int definitionVersion;

    @Column(name = "fingerprint_version", nullable = false)
    private int fingerprintVersion;

    @Column(name = "input_fingerprint", nullable = false, length = 128)
    private String inputFingerprint;

    @Column(name = "execution_id", nullable = false, length = 64)
    private String executionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WorkflowStartReceiptEntity() {
    }

    public static WorkflowStartReceiptEntity empty() {
        return new WorkflowStartReceiptEntity();
    }

    public String getCallerScope() { return callerScope; }
    public void setCallerScope(String callerScope) { this.callerScope = callerScope; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    public int getDefinitionVersion() { return definitionVersion; }
    public void setDefinitionVersion(int definitionVersion) { this.definitionVersion = definitionVersion; }
    public int getFingerprintVersion() { return fingerprintVersion; }
    public void setFingerprintVersion(int fingerprintVersion) { this.fingerprintVersion = fingerprintVersion; }
    public String getInputFingerprint() { return inputFingerprint; }
    public void setInputFingerprint(String inputFingerprint) { this.inputFingerprint = inputFingerprint; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static final class Pk implements Serializable {
        private String callerScope;
        private String idempotencyKey;

        public Pk() { }
        public Pk(String callerScope, String idempotencyKey) {
            this.callerScope = callerScope;
            this.idempotencyKey = idempotencyKey;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Pk pk)) return false;
            return Objects.equals(callerScope, pk.callerScope)
                    && Objects.equals(idempotencyKey, pk.idempotencyKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(callerScope, idempotencyKey);
        }
    }
}
