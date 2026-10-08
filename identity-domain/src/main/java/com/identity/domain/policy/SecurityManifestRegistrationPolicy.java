package com.identity.domain.policy;

import com.grab.framework.security.ScopeDeclaration;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.grab.framework.security.SecurityManifest;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.security.CatalogAuthority;
import com.identity.domain.security.CatalogModule;
import com.identity.domain.security.CatalogScope;
import com.identity.domain.security.SecurityManifestDecision;
import com.identity.domain.security.SecurityManifestCandidateStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SecurityManifestRegistrationPolicy {
    private SecurityManifestRegistrationPolicy() {
    }

    public static SecurityManifestDecision evaluate(SecurityManifest manifest, Map<String, CatalogScope> scopes,
            Map<String, CatalogAuthority> authorities, Map<String, CatalogModule> modules) {
        if (manifest.schemaVersion() != SecurityManifest.CURRENT_SCHEMA_VERSION) {
            var error = new IdentityDomainError.SecurityManifestUnsupportedSchema(manifest.schemaVersion());
            return reject(error);
        }
        String owner = manifest.moduleKey();
        if ("global".equals(owner)) {
            var error = new IdentityDomainError.SecurityManifestProtectedModule(owner);
            return reject(error);
        }
        Map<String, String> parents = new HashMap<>();
        scopes.values().forEach(scope -> parents.put(scope.key(), scope.parent()));
        Set<String> declaredScopes = new HashSet<>();
        Set<String> dependencyKeys = new HashSet<>();
        manifest.dependencies().forEach(dependency -> dependencyKeys.add(dependency.scopeKey()));
        for (var declaration : manifest.scopes()) {
            String key = declaration.scopeKey();
            if (!key.matches("[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)+")) {
                var error = new IdentityDomainError.InvalidScopeKey(key);
                return reject(error);
            }
            if (key.equals(declaration.parentScopeKey())) {
                var error = new IdentityDomainError.SecurityManifestInvalidScopeParent(key);
                return reject(error);
            }
            declaredScopes.add(key);
            CatalogScope existing = scopes.get(key);
            if (existing != null && !owner.equals(existing.owner())) {
                var error = new IdentityDomainError.SecurityManifestScopeOwnerConflict(key);
                return reject(error);
            }
            if (existing != null && !Objects.equals(existing.parent(), declaration.parentScopeKey())) {
                var error = new IdentityDomainError.SecurityManifestInvalidScopeParent(key);
                return reject(error);
            }
            if (existing != null && existing.lifecycle() == Lifecycle.RETIRED
                    && declaration.lifecycle() == Lifecycle.ACTIVE) {
                var error = new IdentityDomainError.SecurityManifestReactivation(key);
                return reject(error);
            }
            parents.put(key, declaration.parentScopeKey());
        }
        for (CatalogScope existing : scopes.values()) {
            if (owner.equals(existing.owner()) && !declaredScopes.contains(existing.key())) {
                var error = new IdentityDomainError.SecurityManifestOmittedScope(owner);
                return reject(error);
            }
        }
        Set<String> declaredCodes = new HashSet<>();
        for (var declaration : manifest.authorities()) {
            String code = declaration.code();
            if (!code.matches("[A-Z][A-Z0-9_]*")) {
                var error = new IdentityDomainError.InvalidAuthorityCode(code);
                return reject(error);
            }
            declaredCodes.add(code);
            CatalogAuthority existing = authorities.get(code);
            if (existing != null && !owner.equals(existing.owner())) {
                var error = new IdentityDomainError.SecurityManifestAuthorityOwnerConflict(code);
                return reject(error);
            }
            if (existing != null && existing.lifecycle() == Lifecycle.RETIRED
                    && declaration.lifecycle() == Lifecycle.ACTIVE) {
                var error = new IdentityDomainError.SecurityManifestReactivation(code);
                return reject(error);
            }
        }
        for (CatalogAuthority existing : authorities.values()) {
            if (owner.equals(existing.owner()) && !declaredCodes.contains(existing.code())) {
                var error = new IdentityDomainError.SecurityManifestOmittedAuthority(owner);
                return reject(error);
            }
        }
        for (String key : parents.keySet()) {
            Set<String> visited = new HashSet<>();
            String current = key;
            while (current != null) {
                if (!visited.add(current)) {
                    var error = new IdentityDomainError.SecurityManifestScopeCycle(key);
                    return reject(error);
                }
                current = parents.get(current);
            }
        }
        for (ScopeDeclaration declaration : manifest.scopes()) {
            String parent = declaration.parentScopeKey();
            if (parent != null && !"global".equals(parent) && !parents.containsKey(parent)
                    && !dependencyKeys.contains(parent)) {
                var error = new IdentityDomainError.SecurityManifestUnknownParentScope(parent);
                return reject(error);
            }
        }
        for (ScopeDeclaration declaration : manifest.scopes()) {
            String parent = declaration.parentScopeKey();
            if (declaration.lifecycle() == Lifecycle.ACTIVE && parent != null && !declaredScopes.contains(parent)
                    && hasRetiredAncestor(parent, scopes)) {
                var error = new IdentityDomainError.SecurityManifestInvalidDependency(parent);
                return reject(error);
            }
        }
        for (var dependency : manifest.dependencies()) {
            String key = dependency.scopeKey();
            CatalogScope existing = scopes.get(key);
            boolean owned = existing != null && owner.equals(existing.owner());
            if (!key.matches("[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)+") || declaredScopes.contains(key) || owned
                    || hasRetiredAncestor(key, scopes)) {
                var error = new IdentityDomainError.SecurityManifestInvalidDependency(key);
                return reject(error);
            }
        }
        for (var dependency : manifest.dependencies()) {
            CatalogScope scope = scopes.get(dependency.scopeKey());
            CatalogModule module = scope == null ? null : modules.get(scope.owner());
            if (scope == null || module == null || module.appliedRevision() < dependency.minimumRevision()
                    || !isEffective(scope.key(), scopes)) {
                var error = new IdentityDomainError.SecurityManifestMissingDependency(dependency.scopeKey());
                return new SecurityManifestDecision(SecurityManifestCandidateStatus.WAITING_DEPENDENCY, error, false, false);
            }
        }
        for (ScopeDeclaration declaration : manifest.scopes()) {
            String parent = declaration.parentScopeKey();
            if (declaration.lifecycle() == Lifecycle.ACTIVE && parent != null && !"global".equals(parent)
                    && !declaredScopes.contains(parent) && !isEffective(parent, scopes)) {
                var error = new IdentityDomainError.SecurityManifestMissingDependency(parent);
                return new SecurityManifestDecision(SecurityManifestCandidateStatus.WAITING_DEPENDENCY, error, false, false);
            }
        }
        return new SecurityManifestDecision(SecurityManifestCandidateStatus.APPLIED, null, true, false);
    }

    public static boolean isEffective(String key, Map<String, CatalogScope> scopes) {
        if (key == null || key.isBlank()) return false;
        Set<String> visited = new HashSet<>();
        String current = key;
        while (current != null && !"global".equals(current)) {
            CatalogScope scope = scopes.get(current);
            if (!visited.add(current) || scope == null || !scope.isEffective()) {
                return false;
            }
            current = scope.parent();
        }
        return true;
    }

    private static boolean hasRetiredAncestor(String key, Map<String, CatalogScope> scopes) {
        Set<String> visited = new HashSet<>();
        String current = key;
        while (current != null && visited.add(current)) {
            CatalogScope scope = scopes.get(current);
            if (scope == null) return false;
            if (scope.lifecycle() == Lifecycle.RETIRED) return true;
            current = scope.parent();
        }
        return false;
    }

    private static SecurityManifestDecision reject(IdentityDomainError error) {
        return new SecurityManifestDecision(SecurityManifestCandidateStatus.QUARANTINED, error, false, false);
    }
}
