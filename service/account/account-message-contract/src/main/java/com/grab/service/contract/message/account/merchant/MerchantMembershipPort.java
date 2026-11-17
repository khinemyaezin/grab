package com.grab.service.contract.message.account.merchant;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;

/**
 * Universal Java Port Interface for Merchant Team Membership and Roles.
 * Replaces legacy MerchantMemberController.
 */
public interface MerchantMembershipPort extends Remote {

    MemberMessageResult inviteMember(InviteMemberMessage message) throws RemoteException;

    MemberMessageResult acceptInvitation(Id merchantId, Id memberId, Id actorUserId) throws RemoteException;

    MemberMessageResult updateMemberRole(Id merchantId, Id memberId, Id actorId, String newRole) throws RemoteException;

    MemberMessageResult removeMember(Id merchantId, Id memberId, Id actorId) throws RemoteException;

    List<MemberMessageResult> listMembers(Id merchantId) throws RemoteException;

    record InviteMemberMessage(
            Id merchantId,
            Id actorId,
            Id targetUserId,
            String role,
            Instant expiresAt
    ) implements Serializable {}

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
    ) implements Serializable {}
}
