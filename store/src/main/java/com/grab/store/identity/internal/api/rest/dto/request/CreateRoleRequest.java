package com.grab.store.identity.internal.api.rest.dto.request;

import jakarta.validation.constraints.*;

public record CreateRoleRequest(
        @NotBlank
        @Pattern(regexp = "[A-Z][A-Z0-9_]*")
        String code,

        @NotBlank
        String name,
        String description
) {
}
