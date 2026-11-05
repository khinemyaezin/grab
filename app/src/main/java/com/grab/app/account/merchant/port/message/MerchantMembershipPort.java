package com.grab.app.account.merchant.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;

/**
 * Universal Java Port Interface for Merchant Team Membership and Roles.
 * Replaces legacy MerchantMemberController.
 */
public interface MerchantMembershipPort {

    MemberMessageResult inviteMember(InviteMemberMessage message);

    MemberMessageResult acceptInvitation(Id merchantId, Id memberId, Id actorUserId);

    MemberMessageResult updateMemberRole(Id merchantId, Id memberId, Id actorId, String newRole);

    MemberMessageResult removeMember(Id merchantId, Id memberId, Id actorId);

    List<MemberMessageResult> listMembers(Id merchantId);

    record InviteMemberMessage(
            Id merchantId,
            Id actorId,
            Id targetUserId,
            String role,
            Instant expiresAt
    ) {}

    record MemberMessageResult(
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
    ) {}
}
