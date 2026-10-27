package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record GrantAccessRequest(
        @NotBlank String userId,
        @NotBlank String roleCode,
        @NotBlank String scopeKey,
        @NotBlank String scopeId,
        @Future Instant expiresAt
) {
}
