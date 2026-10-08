package com.identity.domain.security;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.ScopeDeclaration;
import com.grab.framework.security.SecurityManifest;
import com.identity.domain.exception.IdentityDomainError;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SecurityManifestValidator {
    private SecurityManifestValidator() {
    }

    public static Optional<IdentityDomainError> validate(SecurityManifest candidate, Set<String> knownScopes,
                                                         Set<String> ownedScopes,
                                                         Set<String> ownedAuthorities) {
        var graph = new HashMap<String, String>();
        for (String scope : knownScopes) {
            graph.put(scope, null);
        }
        return validate(candidate, graph, ownedScopes, ownedAuthorities);
    }

    public static Optional<IdentityDomainError> validate(SecurityManifest candidate, Map<String, String> knownGraph,
                                                         Set<String> ownedScopes, Set<String> ownedAuthorities) {
        if (candidate.schemaVersion() > SecurityManifest.CURRENT_SCHEMA_VERSION) {
            return Optional.of(new IdentityDomainError.SecurityManifestUnsupportedSchema(candidate.schemaVersion()));
        }
        if ("global".equals(candidate.moduleKey())) {
            return Optional.of(new IdentityDomainError.SecurityManifestProtectedModule(candidate.moduleKey()));
        }
        var parents = new HashMap<>(knownGraph);
        parents.putIfAbsent("global", null);
        var candidateKeys = new HashSet<String>();
        for (ScopeDeclaration scope : candidate.scopes()) {
            candidateKeys.add(scope.scopeKey());
        }
        for (ScopeDeclaration scope : candidate.scopes()) {
            String scopeKey = scope.scopeKey();
            String parentScopeKey = scope.parentScopeKey();
            if ("global".equals(scopeKey) || scopeKey.equals(parentScopeKey)) {
                return Optional.of(new IdentityDomainError.SecurityManifestInvalidScopeParent(scopeKey));
            }
            if (parentScopeKey != null
                    && !candidateKeys.contains(parentScopeKey)
                    && !knownGraph.containsKey(parentScopeKey)) {
                return Optional.of(new IdentityDomainError.SecurityManifestUnknownParentScope(parentScopeKey));
            }
            if (ownedScopes.contains(scopeKey)) {
                return Optional.of(new IdentityDomainError.SecurityManifestScopeOwnerConflict(scopeKey));
            }
            parents.put(scopeKey, parentScopeKey);
        }
        for (String key : parents.keySet()) {
            var visited = new HashSet<String>();
            String current = key;
            while (current != null) {
                if (!visited.add(current)) {
                    return Optional.of(new IdentityDomainError.SecurityManifestScopeCycle(key));
                }
                current = parents.getOrDefault(current, null);
            }
        }
        for (AuthorityDefinition authority : candidate.authorities()) {
            String code = authority.code();
            if (ownedAuthorities.contains(code)) {
                return Optional.of(new IdentityDomainError.SecurityManifestAuthorityOwnerConflict(code));
            }
        }
        return Optional.empty();
    }
}
