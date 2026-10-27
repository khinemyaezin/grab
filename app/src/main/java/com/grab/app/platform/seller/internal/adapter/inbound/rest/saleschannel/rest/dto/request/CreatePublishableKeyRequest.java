package com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePublishableKeyRequest(
        @NotBlank
        @Size(max = 100)
        String label
) {
}
