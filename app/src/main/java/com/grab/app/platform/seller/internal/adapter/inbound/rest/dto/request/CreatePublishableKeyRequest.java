package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePublishableKeyRequest(
        @NotBlank(message = "Label is required")
        String label
) {}

