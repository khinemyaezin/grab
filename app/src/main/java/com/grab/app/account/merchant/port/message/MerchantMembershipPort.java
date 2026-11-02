package com.grab.app.account.merchant.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;

/**
 * Universal Java Port Interface for Merchant Team Membership and Roles.
 * Replaces legacy MerchantMemberController.
 */
public interface MerchantMembershipPort {

    MemberResult inviteMember(Id merchantId, InviteMemberMessage message);

    void acceptInvitation(Id memberId, Id userId);

    void updateMemberRole(Id memberId, String roleCode);

    void removeMember(Id memberId);

    List<MemberResult> listMembers(Id merchantId);

    record InviteMemberMessage(
            String email,
            String roleCode
    ) {}

    record MemberResult(
            Id id,
            Id merchantId,
            Id userId,
            String email,
            String roleCode,
            String status,
            Instant joinedAt
    ) {}
}

