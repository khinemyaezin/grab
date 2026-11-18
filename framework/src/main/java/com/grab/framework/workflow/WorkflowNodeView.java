package com.grab.framework.workflow;

import java.time.Instant;

public record WorkflowNodeView(
        String executionId,
        String nodeId,
        String branchId,
        String status,
        int attempts,
        Instant deadlineAt,
        Instant retryAt,
        String errorMessage
) {
}
