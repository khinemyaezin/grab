package com.grab.service.contract.message.workflow;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.time.Instant;

public interface WorkflowRuntimeCommandPort extends Remote {

    StartResult start(StartMessage message) throws RemoteException;

    OperationResult requestCancellation(CancelMessage message) throws RemoteException;

    OperationResult retryIncident(RetryIncidentMessage message) throws RemoteException;

    record StartMessage(
            String definitionName,
            int definitionVersion,
            int payloadVersion,
            String callerScope,
            String inputJson,
            String idempotencyKey
    ) implements Serializable {
    }

    record CancelMessage(Id executionId, String callerScope, String reason) implements Serializable {
    }

    record RetryIncidentMessage(Id executionId, String callerScope, String reason) implements Serializable {
    }

    record StartResult(
            Id executionId,
            String definitionName,
            int definitionVersion,
            String status,
            Instant startedAt
    ) implements Serializable {
    }

    record OperationResult(Id executionId, String status) implements Serializable {
    }
}
