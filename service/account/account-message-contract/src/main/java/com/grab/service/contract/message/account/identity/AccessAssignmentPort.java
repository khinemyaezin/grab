package com.grab.service.contract.message.account.identity;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Universal Java Port Interface for Access Assignments, Invitations, and Context Switching.
 * Unifies AccessAdminController, AccessContextController, and AccessManagementPort.
 */
public interface AccessAssignmentPort extends Remote {

    AssignmentMessageResult assignAccess(AssignAccessMessage message) throws RemoteException;

    AssignmentMessageResult changeAssignmentStatus(ChangeAssignmentStatusMessage message) throws RemoteException;

    AuthTokenResult switchAccessContext(SwitchContextMessage message) throws RemoteException;

    List<AssignmentMessageResult> listAssignmentsForUser(Id userId, String actorScopeKey, String actorScopeId) throws RemoteException;

    List<ContextMessageResult> listAvailableContexts(Id userId) throws RemoteException;

    AccessWriteAcknowledgement replaceAccess(ReplaceAccessMessage message) throws RemoteException;

    AccessWriteAcknowledgement revokeAccess(RevokeAccessMessage message) throws RemoteException;

    AccessWriteAcknowledgement revokeSessionsByScope(RevokeSessionsByScopeMessage message) throws RemoteException;

    record ReplaceAccessMessage(
            Id userId, String previousRoleCode, String replacementRoleCode,
            String scopeKey, String scopeId, Set<String> authorityCodes
    ) implements Serializable {
        public ReplaceAccessMessage {
            authorityCodes = authorityCodes == null ? Set.of() : Set.copyOf(authorityCodes);
        }
    }

    record RevokeAccessMessage(Id userId, String roleCode, String scopeKey, String scopeId) implements Serializable {}

    record RevokeSessionsByScopeMessage(String scopeKey, String scopeId) implements Serializable {}

    record AccessWriteAcknowledgement() implements Serializable {}

    record AssignAccessMessage(
            Id userId,
            String roleCode,
            String scopeKey,
            String scopeId,
            Instant expiresAt,
            Id assignedBy,
            String actorScopeKey,
            String actorScopeId,
            Set<String> actorRoleCodes
    ) implements Serializable {}

    record ChangeAssignmentStatusMessage(
            Id assignmentId,
            String requestedStatus,
            String actorScopeKey,
            String actorScopeId,
            Id actorUserId,
            Set<String> actorRoleCodes
    ) implements Serializable {}

    record SwitchContextMessage(
            Id userId,
            Id targetAssignmentId,
            String currentRefreshToken
    ) implements Serializable {}

    record AssignmentMessageResult(
            String id,
            String userId,
            String roleCode,
            String scopeKey,
            String scopeId,
            String status,
            String assignedBy,
            String createdAt,
            String updatedAt,
            String expiresAt
    ) implements Serializable {}

    record ContextMessageResult(
            String assignmentId,
            Set<String> roleCodes,
            String scopeKey,
            String scopeId,
            String expiresAt,
            String title,
            String subtitle
    ) implements Serializable {}

    record AuthTokenResult(
            String accessToken,
            String refreshToken,
            long expiresInMs,
            String userId,
            String email,
            Set<String> roles,
            String status,
            boolean contextSelectionRequired
    ) implements Serializable {}
}
