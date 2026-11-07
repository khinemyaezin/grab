package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

import java.time.Instant;

public record WorkflowExecutionResponse(
        String workflowId,
        String workflowName,
        String status,
        String targetEntityId,
        String currentStep,
        Instant startedAt,
        Instant completedAt
) {}

