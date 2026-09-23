package com.merchant.application.model.write;

import com.merchant.application.model.read.MerchantMemberView;
import com.merchant.domain.aggregate.MerchantMember;

import java.time.Instant;

public record MerchantMemberResult(
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
    public static MerchantMemberResult from(MerchantMember member) {
        return new MerchantMemberResult(
                member.getId().getValue(),
                member.getMerchantId().getValue(),
                member.getUserId().getValue(),
                member.getRole().name(),
                member.getStatus().name(),
                member.getInvitedBy() == null ? null : member.getInvitedBy().getValue(),
                member.getInvitationExpiresAt(),
                member.getJoinedAt(),
                member.getCreatedAt(),
                member.getUpdatedAt(),
                member.getVersion()
        );
    }

    public static MerchantMemberResult from(MerchantMemberView view) {
        return new MerchantMemberResult(
                view.memberId(),
                view.merchantId(),
                view.userId(),
                view.role(),
                view.status(),
                view.invitedBy(),
                view.invitationExpiresAt(),
                view.joinedAt(),
                view.createdAt(),
                view.updatedAt(),
                view.version()
        );
    }
}
