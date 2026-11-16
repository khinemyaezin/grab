package com.grab.app.workflow.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WorkflowRuntimeQueryPort {

    Optional<WorkflowExecutionSummary> getExecution(Id executionId, String callerScope);

    List<WorkflowNodeView> listNodes(Id executionId, String callerScope);

    List<WorkflowHistoryEntry> listHistory(Id executionId, String callerScope, int page, int size);

    List<WorkflowIncidentView> listIncidents(String callerScope, int page, int size);

    record WorkflowExecutionSummary(
            Id executionId,
            String definitionName,
            int definitionVersion,
            String status,
            boolean cancellationRequested,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    record WorkflowNodeView(
            Id executionId,
            String nodeId,
            String branchId,
            String status,
            int attempts,
            Instant deadlineAt,
            Instant retryAt,
            String errorMessage
    ) {
    }

    record WorkflowHistoryEntry(
            Id executionId,
            long sequenceNumber,
            String nodeId,
            String eventType,
            Instant occurredAt
    ) {
    }

    record WorkflowIncidentView(
            Id executionId,
            String definitionName,
            String nodeId,
            String status,
            String reason,
            Instant occurredAt
    ) {
    }
}
