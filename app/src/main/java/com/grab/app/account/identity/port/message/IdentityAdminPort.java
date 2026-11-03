package com.grab.app.account.identity.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for User Administration and Profile Lookups.
 * Unifies UserAdminController and UserProfileQuery.
 */
public interface IdentityAdminPort {

    Optional<UserDetailResult> findUserById(Id userId);

    List<UserSummaryResult> listUsers(int page, int size);

    void changeUserStatus(Id userId, String status);

    record UserDetailResult(
            String id,
            String email,
            String status,
            String createdAt,
            List<AccessContextInfo> accessContexts
    ) {
        public record AccessContextInfo(
                String assignmentId,
                String roleCode,
                String scopeKey,
                String scopeId,
                String status
        ) {}
    }

    record UserSummaryResult(
            String id,
            String email,
            String status,
            String createdAt
    ) {}
}
