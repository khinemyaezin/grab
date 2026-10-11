package com.identity.application.model.read;

public record RoleDeclarationStatusView(
        String roleCode,
        String owner,
        int appliedRevision,
        String appliedDigest,
        int latestRevision,
        String latestStatus,
        String dependencyReason,
        long conflictCount
) {
}
