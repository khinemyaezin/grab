package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response;

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
        long version,
        String accessProvisioningStatus,
        String accessProvisioningError
) {
    public MerchantMemberResponse(
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
