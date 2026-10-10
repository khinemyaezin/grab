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
        long version,
        String accessProvisioningStatus,
        String accessProvisioningError
) {
    public MerchantMemberView(
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
        this(memberId, merchantId, userId, role, status, invitedBy, invitationExpiresAt, joinedAt, createdAt, updatedAt, version, null, null);
    }
}
