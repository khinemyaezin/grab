package com.grab.store.identity.internal.api.rest.dto.request;

import com.identity.domain.enums.AccessScopeType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateAccessInvitationRequest(
        @Email @NotBlank String inviteeEmail,
        @NotBlank String platformCode,
        @NotBlank String roleCode,
        @NotNull AccessScopeType scopeType,
        @NotBlank String scopeId,
        @NotNull @Future Instant expiresAt
) {
}
