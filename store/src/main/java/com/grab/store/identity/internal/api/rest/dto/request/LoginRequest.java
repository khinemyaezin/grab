package com.grab.store.identity.internal.api.rest.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @Email @NotBlank
        String email,
        @NotBlank
        String password
) {
}
