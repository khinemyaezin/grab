package com.grab.app.channeldiscovery.saleschannel.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePublishableKeyRequest(
        @NotBlank
        @Size(max = 100)
        String label
) {
}
