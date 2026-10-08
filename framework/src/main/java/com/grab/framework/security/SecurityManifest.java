package com.grab.framework.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

/** Complete, immutable security declaration published by one bounded context. */
public record SecurityManifest(
        int schemaVersion,
        String moduleKey,
        int securityRevision,
        List<ScopeDeclaration> scopes,
        List<AuthorityDefinition> authorities,
        List<SecurityDependency> dependencies
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public SecurityManifest {
        Objects.requireNonNull(moduleKey, "module key is required");
        Objects.requireNonNull(scopes, "scopes are required");
        Objects.requireNonNull(authorities, "authorities are required");
        Objects.requireNonNull(dependencies, "dependencies are required");
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schema version must be positive");
        }
        moduleKey = moduleKey.trim().toLowerCase(java.util.Locale.ROOT);
        if (moduleKey.isBlank()) {
            throw new IllegalArgumentException("module key must not be blank");
        }
        if (securityRevision < 1) {
            throw new IllegalArgumentException("security revision must be positive");
        }
        scopes = List.copyOf(scopes);
        authorities = List.copyOf(authorities);
        dependencies = List.copyOf(dependencies);
        var scopeKeys = new HashSet<String>();
        scopes.forEach(scope -> {
            Objects.requireNonNull(scope, "scope declaration is required");
            if (!scopeKeys.add(scope.scopeKey())) {
                throw new IllegalArgumentException("duplicate scope key: " + scope.scopeKey());
            }
        });
        var authorityCodes = new HashSet<String>();
        authorities.forEach(authority -> {
            Objects.requireNonNull(authority, "authority definition is required");
            if (!authorityCodes.add(authority.code())) {
                throw new IllegalArgumentException("duplicate authority code: " + authority.code());
            }
        });
        var dependencyKeys = new HashSet<String>();
        dependencies.forEach(dependency -> {
            Objects.requireNonNull(dependency, "dependency is required");
            if (!dependencyKeys.add(dependency.scopeKey())) {
                throw new IllegalArgumentException("duplicate dependency: " + dependency.scopeKey());
            }
        });
    }

    public SecurityManifest(String moduleKey, int securityRevision,
                            List<ScopeDeclaration> scopes,
                            List<AuthorityDefinition> authorities) {
        this(moduleKey, securityRevision, scopes, authorities, List.of());
    }

    /** Compatibility constructor accepting the original dependency-only scope keys. */
    public SecurityManifest(String moduleKey, int securityRevision,
                            List<ScopeDeclaration> scopes,
                            List<AuthorityDefinition> authorities,
                            List<?> dependencies) {
        this(CURRENT_SCHEMA_VERSION, moduleKey, securityRevision, scopes, authorities,
                dependencies.stream().map(value -> value instanceof SecurityDependency dependency
                        ? dependency
                        : new SecurityDependency(String.valueOf(value))).toList());
    }

    public SecurityManifest(String moduleKey, int securityRevision, int schemaVersion,
                            List<ScopeDeclaration> scopes,
                            List<AuthorityDefinition> authorities,
                            List<SecurityDependency> dependencies) {
        this(schemaVersion, moduleKey, securityRevision, scopes, authorities, dependencies);
    }

    /** Canonical digest of semantic content; declaration order and diagnostics are excluded. */
    public String contentDigest() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            update(digest, moduleKey);
            update(digest, Integer.toString(securityRevision));
            update(digest, Integer.toString(schemaVersion));
            scopes.stream()
                    .sorted(Comparator.comparing(ScopeDeclaration::scopeKey))
                    .forEach(scope -> {
                        update(digest, scope.scopeKey());
                        update(digest, scope.parentScopeKey());
                        update(digest, scope.lifecycle().name());
                    });
            authorities.stream()
                    .sorted(Comparator.comparing(AuthorityDefinition::code))
                    .forEach(authority -> {
                        update(digest, authority.code());
                        update(digest, authority.category());
                        update(digest, authority.name());
                        update(digest, authority.description());
                        update(digest, authority.lifecycle().name());
                    });
            dependencies.stream().sorted(Comparator.comparing(SecurityDependency::scopeKey)).forEach(dependency -> {
                update(digest, dependency.scopeKey());
                update(digest, Integer.toString(dependency.minimumRevision()));
            });
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void update(MessageDigest digest, String value) {
        if (value == null) {
            digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(-1).array());
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
        digest.update(bytes);
    }
}
