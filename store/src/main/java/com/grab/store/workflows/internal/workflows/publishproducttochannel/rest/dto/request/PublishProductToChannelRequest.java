package com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PublishProductToChannelRequest(
        @NotBlank String productId,
        @NotBlank String salesChannelId,
        String idempotencyKey
) {
}
