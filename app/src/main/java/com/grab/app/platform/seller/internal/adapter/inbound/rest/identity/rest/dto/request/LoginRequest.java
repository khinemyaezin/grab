package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @Email @NotBlank
        String email,

        @NotBlank
        String password
) {
}
