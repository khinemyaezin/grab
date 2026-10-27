package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AcceptAccessInvitationRequest(
        @NotBlank String acceptanceToken
) {
}
