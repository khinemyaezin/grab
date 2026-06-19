package com.grab.store.identity.internal.api.rest.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @Email @NotBlank
        String email,

        @NotBlank @Size(min = 8, max = 128)
        String password,

        @NotBlank String role
) {
}
