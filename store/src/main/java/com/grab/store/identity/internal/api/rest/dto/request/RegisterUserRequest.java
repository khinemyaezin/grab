package com.grab.store.identity.internal.api.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @Email @NotBlank
        String email,

        @NotBlank @Size(min = 8, max = 128)
        String password,

        @NotBlank @Pattern(regexp = "CUSTOMER|SELLER")
        String role
) {
}
