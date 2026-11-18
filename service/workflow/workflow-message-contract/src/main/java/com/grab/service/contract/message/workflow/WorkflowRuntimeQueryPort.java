package com.grab.service.contract.message.workflow;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;

public interface WorkflowRuntimeQueryPort extends Remote {

    WorkflowExecutionSummary getExecution(Id executionId, String callerScope) throws RemoteException;

    List<WorkflowNodeView> listNodes(Id executionId, String callerScope) throws RemoteException;

    List<WorkflowHistoryEntry> listHistory(Id executionId, String callerScope, int page, int size) throws RemoteException;

    List<WorkflowIncidentView> listIncidents(String callerScope, int page, int size) throws RemoteException;

    record WorkflowExecutionSummary(
            Id executionId,
            String definitionName,
            int definitionVersion,
            String status,
            boolean cancellationRequested,
            Instant createdAt,
            Instant updatedAt
    ) implements Serializable {
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
    ) implements Serializable {
    }

    record WorkflowHistoryEntry(
            Id executionId,
            long sequenceNumber,
            String nodeId,
            String eventType,
            Instant occurredAt
    ) implements Serializable {
    }

    record WorkflowIncidentView(
            Id executionId,
            String definitionName,
            String nodeId,
            String status,
            String reason,
            Instant occurredAt
    ) implements Serializable {
    }
}
