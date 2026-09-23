package com.grab.store.merchant.internal.api.rest.dto.response;

import java.time.Instant;

public record MerchantMemberResponse(
        String memberId,
        String merchantId,
        String userId,
        String role,
        String status,
        String invitedBy,
        Instant invitationExpiresAt,
        Instant joinedAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
