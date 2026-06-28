package com.grab.store.identity.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AcceptAccessInvitationRequest(
        @NotBlank String acceptanceToken
) {
}
