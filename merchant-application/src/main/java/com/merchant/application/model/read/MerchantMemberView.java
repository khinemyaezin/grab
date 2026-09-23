package com.merchant.application.model.read;

import java.time.Instant;

public record MerchantMemberView(
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
