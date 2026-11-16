package com.grab.app.workflow.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;

public interface WorkflowRuntimeCommandPort {

    StartResult start(StartMessage message);

    OperationResult requestCancellation(CancelMessage message);

    OperationResult retryIncident(RetryIncidentMessage message);

    record StartMessage(
            String definitionName,
            int definitionVersion,
            int payloadVersion,
            String callerScope,
            String inputJson,
            String idempotencyKey
    ) {
    }

    record CancelMessage(Id executionId, String callerScope, String reason) {
    }

    record RetryIncidentMessage(Id executionId, String callerScope, String reason) {
    }

    record StartResult(
            Id executionId,
            String definitionName,
            int definitionVersion,
            String status,
            Instant startedAt
    ) {
    }

    record OperationResult(Id executionId, String status) {
    }
}
