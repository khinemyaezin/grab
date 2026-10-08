package com.identity.domain.port.outbound;

import com.grab.framework.security.ScopeDeclaration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Durable identity-side port for the module scope catalog. */
public interface ScopeManifestRepository {
    /**
     * Applies a complete module snapshot. Implementations must be idempotent and
     * must return false for stale or conflicting revisions.
     */
    boolean apply(String moduleKey, int manifestVersion, List<ScopeDeclaration> scopes);

    default List<ScopeDeclaration> loadActive() {
        return List.of();
    }

    default Map<String, String> loadGraph() {
        return loadActive().stream().collect(Collectors.toUnmodifiableMap(
                ScopeDeclaration::scopeKey, ScopeDeclaration::parentScopeKey, (left, right) -> left));
    }

    default Map<String, String> loadOwners() {
        return Map.of();
    }
}
