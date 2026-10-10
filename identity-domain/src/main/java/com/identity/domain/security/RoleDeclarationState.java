package com.identity.domain.security;

import java.util.Objects;

public record RoleDeclarationState(
        String roleCode,
        String owner,
        String assignmentScopeKey,
        int appliedRevision,
        String appliedDigest
) {
    public RoleDeclarationState {
        Objects.requireNonNull(roleCode, "roleCode is required");
        Objects.requireNonNull(owner, "owner is required");
        Objects.requireNonNull(assignmentScopeKey, "assignmentScopeKey is required");
        if (appliedRevision < 0) {
            throw new IllegalArgumentException("appliedRevision cannot be negative");
        }
        appliedDigest = appliedDigest == null ? "" : appliedDigest;
    }

    public RoleDeclarationState applied(int revision, String digest, String assignmentScopeKey) {
        if (revision <= appliedRevision) {
            throw new IllegalArgumentException("applied revision must increase");
        }
        return new RoleDeclarationState(roleCode, owner, assignmentScopeKey, revision, digest);
    }
}
