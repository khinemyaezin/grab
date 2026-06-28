package com.grab.store.identity.internal.api.rest.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record GrantAccessRequest(
        @NotBlank String userId,
        @NotBlank String platformCode,
        @NotBlank String roleCode,
        @NotBlank String scopeKey,
        @NotBlank String scopeId,
        @Future Instant expiresAt
) {
}
