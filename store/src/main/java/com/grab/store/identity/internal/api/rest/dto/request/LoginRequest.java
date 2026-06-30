package com.grab.store.identity.internal.api.rest.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @Email @NotBlank
        String email,

        @NotBlank
        String password,

        @Pattern(regexp = "[A-Z][A-Z0-9_]*")
        String platformCode,

        @Pattern(regexp = "(?s).*\\S.*")
        @Size(max = 255)
        String assignmentId
) {
}
