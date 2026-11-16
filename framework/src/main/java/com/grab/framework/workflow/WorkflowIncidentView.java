package com.grab.framework.workflow;

import java.time.Instant;

public record WorkflowIncidentView(
        String executionId,
        String definitionName,
        String nodeId,
        String status,
        String reason,
        Instant occurredAt
) {
}
