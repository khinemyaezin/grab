package com.grab.framework.security.role;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HashMap;
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
        Map<String, Integer> normalizedRevisions = new HashMap<>();
        minimumOwnerRevisions.forEach((moduleKey, revision) -> {
            if (moduleKey == null || moduleKey.isBlank() || revision == null || revision < 1) {
                throw new IllegalArgumentException("minimum owner revisions must have nonblank keys and positive values");
            }
            String normalizedModuleKey = moduleKey.trim().toLowerCase(Locale.ROOT);
            if (normalizedRevisions.putIfAbsent(normalizedModuleKey, revision) != null) {
                throw new IllegalArgumentException("minimum owner revisions contain duplicate module keys");
            }
        });
        minimumOwnerRevisions = Map.copyOf(normalizedRevisions);
        permissions = List.copyOf(permissions);
        if (permissions.stream().distinct().count() != permissions.size()) {
            throw new IllegalArgumentException("permissions must not contain duplicate references");
        }
    }

    public String contentDigest() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream content = new DataOutputStream(bytes);
            writeString(content, owner);
            writeString(content, roleCode);
            writeString(content, assignmentScopeKey);
            content.writeInt(declarationRevision);
            content.writeInt(minimumOwnerRevisions.size());
            minimumOwnerRevisions.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> writeRevision(content, entry.getKey(), entry.getValue()));
            content.writeInt(permissions.size());
            permissions.stream()
                    .sorted(Comparator.comparing(RolePermissionReference::owner)
                            .thenComparing(RolePermissionReference::code))
                    .forEach(reference -> writePermission(content, reference));
            content.flush();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes.toByteArray()));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate content digest", e);
        }
    }

    public String legacyContentDigest() {
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
                    .forEach(reference -> {
                        digest.update(reference.owner().getBytes(StandardCharsets.UTF_8));
                        digest.update(reference.code().getBytes(StandardCharsets.UTF_8));
                    });
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to calculate legacy content digest", exception);
        }
    }

    private static void writeString(DataOutputStream content, String value) throws IOException {
        byte[] encoded = value.getBytes(StandardCharsets.UTF_8);
        content.writeInt(encoded.length);
        content.write(encoded);
    }

    private static void writeRevision(DataOutputStream content, String owner, int revision) {
        try {
            writeString(content, owner);
            content.writeInt(revision);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to calculate content digest", exception);
        }
    }

    private static void writePermission(DataOutputStream content, RolePermissionReference reference) {
        try {
            writeString(content, reference.owner());
            writeString(content, reference.code());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to calculate content digest", exception);
        }
    }
}
