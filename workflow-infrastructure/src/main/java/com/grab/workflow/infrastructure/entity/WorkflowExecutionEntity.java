package com.grab.workflow.infrastructure.entity;

import com.grab.framework.workflow.ExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "workflow_execution")
public class WorkflowExecutionEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "definition_name", nullable = false, length = 128)
    private String definitionName;

    @Column(name = "definition_version", nullable = false)
    private int definitionVersion;

    @Column(name = "payload_version", nullable = false)
    private int payloadVersion;

    @Column(name = "caller_scope", nullable = false, length = 128)
    private String callerScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ExecutionStatus status;

    @Column(name = "input_json", nullable = false, columnDefinition = "TEXT")
    private String inputJson;

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "outputs_json", columnDefinition = "TEXT")
    private String outputsJson;

    @Column(name = "choices_json", columnDefinition = "TEXT")
    private String choicesJson;

    @Column(name = "accepted_signals_json", columnDefinition = "TEXT")
    private String acceptedSignalsJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "cancellation_requested", nullable = false)
    private boolean cancellationRequested;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "transition_revision", nullable = false)
    private long transitionRevision;

    @Column(name = "fingerprint_recovery_required", nullable = false)
    private boolean fingerprintRecoveryRequired;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected WorkflowExecutionEntity() {
    }

    public static WorkflowExecutionEntity empty() {
        return new WorkflowExecutionEntity();
    }

}
