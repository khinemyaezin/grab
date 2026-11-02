package com.grab.app.account.identity.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Universal Java Port Interface for Access Assignments, Invitations, and Context Switching.
 * Unifies AccessAdminController, AccessContextController, AccessInvitationController, and AccessManagementPort.
 */
public interface AccessAssignmentPort {

    AccessAssignmentResult assignAccess(AssignAccessMessage message);

    void replaceAccess(ReplaceAccessMessage message);

    void suspendAssignment(Id assignmentId);

    void reactivateAssignment(Id assignmentId);

    void revokeAssignment(Id assignmentId);

    void revokeAccess(RevokeAccessMessage message);

    void revokeSessionsByScope(String scopeKey, String scopeId);

    List<AccessAssignmentResult> listAssignmentsForUser(Id userId);

    List<AccessContextResult> listAvailableContexts(Id userId);

    AccessContextResult selectAccessContext(Id userId, Id assignmentId);

    InvitationResult createInvitation(CreateInvitationMessage message);

    void acceptInvitation(String token, Id userId);

    void cancelInvitation(Id invitationId);

    record AssignAccessMessage(
            Id userId,
            String roleCode,
            String scopeKey,
            String scopeId,
            Set<String> authorityCodes
    ) {}

    record ReplaceAccessMessage(
            Id userId,
            String previousRoleCode,
            String roleCode,
            String scopeKey,
            String scopeId,
            Set<String> authorityCodes
    ) {}

    record RevokeAccessMessage(
            Id userId,
            String roleCode,
            String scopeKey,
            String scopeId
    ) {}

    record CreateInvitationMessage(
            String email,
            String roleCode,
            String scopeKey,
            String scopeId
    ) {}

    record AccessAssignmentResult(
            Id id,
            Id userId,
            String roleCode,
            String scopeKey,
            String scopeId,
            String status,
            Set<String> authorityCodes,
            Instant createdAt
    ) {}

    record AccessContextResult(
            Id assignmentId,
            String roleCode,
            String scopeKey,
            String scopeId,
            boolean active
    ) {}

    record InvitationResult(
            Id id,
            String email,
            String roleCode,
            String scopeKey,
            String scopeId,
            String token,
            String status,
            Instant expiresAt
    ) {}
}

