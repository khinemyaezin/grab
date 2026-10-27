package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record InviteMerchantMemberRequest(
        @NotBlank String targetUserId,
        @NotBlank String role,
        Instant expiresAt
) {
}
