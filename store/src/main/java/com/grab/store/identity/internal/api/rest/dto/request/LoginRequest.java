package com.grab.store.identity.internal.api.rest.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @Email @NotBlank
        String email,
        @NotBlank
        String password,
        @Pattern(regexp = "[A-Z][A-Z0-9_]*")
        String platformCode,
        @Size(min = 1, max = 255)
        String assignmentId
) {
    public LoginRequest(String email, String password) {
        this(email, password, null, null);
    }
}
