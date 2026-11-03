package com.grab.app.account.identity.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Universal Java Port Interface for Access Assignments, Invitations, and Context Switching.
 * Unifies AccessAdminController, AccessContextController, and AccessManagementPort.
 */
public interface AccessAssignmentPort {

    AssignmentMessageResult assignAccess(AssignAccessMessage message);

    AssignmentMessageResult changeAssignmentStatus(ChangeAssignmentStatusMessage message);

    AuthTokenResult switchAccessContext(SwitchContextMessage message);

    List<AssignmentMessageResult> listAssignmentsForUser(Id userId, String actorScopeKey, String actorScopeId);

    List<ContextMessageResult> listAvailableContexts(Id userId);

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
    ) {}

    record ChangeAssignmentStatusMessage(
            Id assignmentId,
            String requestedStatus,
            String actorScopeKey,
            String actorScopeId,
            Id actorUserId,
            Set<String> actorRoleCodes
    ) {}

    record SwitchContextMessage(
            Id userId,
            Id targetAssignmentId,
            String currentRefreshToken
    ) {}

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
    ) {}

    record ContextMessageResult(
            String assignmentId,
            Set<String> roleCodes,
            String scopeKey,
            String scopeId,
            String expiresAt,
            String title,
            String subtitle
    ) {}

    record AuthTokenResult(
            String accessToken,
            String refreshToken,
            long expiresInMs,
            String userId,
            String email,
            Set<String> roles,
            String status,
            boolean contextSelectionRequired
    ) {}
}
