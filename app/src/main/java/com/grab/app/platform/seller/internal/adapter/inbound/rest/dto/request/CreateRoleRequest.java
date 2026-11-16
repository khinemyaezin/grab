package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank
        @Pattern(regexp = "[A-Z][A-Z0-9_]*")
        String code,

        @NotBlank
        String name,

        String description,

        @NotEmpty
        Set<@Pattern(regexp = "[A-Z][A-Z0-9_]*") String> authorityCodes
) {}

