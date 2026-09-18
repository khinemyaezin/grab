package com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.dto.response;

import java.time.Instant;

public record PublishProductToChannelResponse(
        String workflowId,
        String status,
        String currentStep,
        String productId,
        String salesChannelId,
        Boolean missingRoute,
        String errorMessage,
        Instant updatedAt
) {
}
