package com.grab.framework.security.role;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public record RoleDeclaration(
        String owner,
        String roleCode,
        String assignmentScopeKey,
        int declarationRevision,
        Map<String, Integer> minimumOwnerRevisions,
        List<RolePermissionReference> permissions
) {
    public RoleDeclaration {
        Objects.requireNonNull(owner, "owner is required");
        Objects.requireNonNull(roleCode, "roleCode is required");
        Objects.requireNonNull(assignmentScopeKey, "assignmentScopeKey is required");
        Objects.requireNonNull(minimumOwnerRevisions, "minimumOwnerRevisions is required");
        Objects.requireNonNull(permissions, "permissions are required");
        if (declarationRevision < 1) {
            throw new IllegalArgumentException("declarationRevision must be positive");
        }
        owner = owner.trim().toLowerCase(Locale.ROOT);
        roleCode = roleCode.trim().toUpperCase(Locale.ROOT);
        assignmentScopeKey = assignmentScopeKey.trim().toLowerCase(Locale.ROOT);
        if (owner.isBlank()) {
            throw new IllegalArgumentException("owner cannot be blank");
        }
        if (roleCode.isBlank()) {
            throw new IllegalArgumentException("roleCode cannot be blank");
        }
        if (assignmentScopeKey.isBlank()) {
            throw new IllegalArgumentException("assignmentScopeKey cannot be blank");
        }
        minimumOwnerRevisions = Map.copyOf(minimumOwnerRevisions);
        permissions = List.copyOf(permissions);
    }

    public String contentDigest() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(owner.getBytes(StandardCharsets.UTF_8));
            digest.update(roleCode.getBytes(StandardCharsets.UTF_8));
            digest.update(assignmentScopeKey.getBytes(StandardCharsets.UTF_8));
            digest.update(Integer.toString(declarationRevision).getBytes(StandardCharsets.UTF_8));
            minimumOwnerRevisions.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        digest.update(entry.getKey().getBytes(StandardCharsets.UTF_8));
                        digest.update(Integer.toString(entry.getValue()).getBytes(StandardCharsets.UTF_8));
                    });
            permissions.stream()
                    .sorted(Comparator.comparing(RolePermissionReference::owner)
                            .thenComparing(RolePermissionReference::code))
                    .forEach(ref -> {
                        digest.update(ref.owner().getBytes(StandardCharsets.UTF_8));
                        digest.update(ref.code().getBytes(StandardCharsets.UTF_8));
                    });
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate content digest", e);
        }
    }
}
