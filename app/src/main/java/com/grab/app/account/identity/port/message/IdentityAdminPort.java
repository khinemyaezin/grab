package com.grab.app.account.identity.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for User Administration and Profile Lookups.
 * Unifies UserAdminController, UserProfileQuery, and IdentityLookupQuery.
 */
public interface IdentityAdminPort {

    Optional<UserDetailResult> findUserById(Id userId);

    Optional<UserDetailResult> findUserByUsername(String username);

    Optional<UserDetailResult> findUserByEmail(String email);

    List<UserSummaryResult> searchUsers(UserSearchCriteria criteria);

    void approveUser(Id userId);

    void suspendUser(Id userId, String reason);

    void reactivateUser(Id userId);

    UserProfileResult getUserProfile(Id userId);

    record UserSearchCriteria(
            String query,
            String status,
            int page,
            int size
    ) {}

    record UserDetailResult(
            Id id,
            String username,
            String email,
            String fullName,
            String status,
            Instant createdAt,
            Instant lastLoginAt
    ) {}

    record UserSummaryResult(
            Id id,
            String username,
            String email,
            String status,
            Instant createdAt
    ) {}

    record UserProfileResult(
            Id userId,
            String username,
            String email,
            String fullName,
            String avatarUrl
    ) {}
}

