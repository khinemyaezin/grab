package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request;

import jakarta.validation.constraints.*;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank
        @Pattern(regexp = "[A-Z][A-Z0-9_]*")
        String code,

        @NotBlank String name,
        String description,
        @NotEmpty Set<@Pattern(regexp = "[A-Z][A-Z0-9_]*") String> authorityCodes
) {
}
