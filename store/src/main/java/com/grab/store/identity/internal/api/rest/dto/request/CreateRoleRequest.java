package com.grab.store.identity.internal.api.rest.dto.request;

import jakarta.validation.constraints.*;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank
        @Pattern(regexp = "[A-Z][A-Z0-9_]*")
        String code,

        @NotBlank String name,
        String description,
        @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]*") String platformCode,
        @NotEmpty Set<@Pattern(regexp = "[A-Z][A-Z0-9_]*") String> authorityCodes
) {
}
