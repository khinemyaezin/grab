package com.identity.domain.valueobject;

import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;

import java.util.Objects;

public record AccessScope(ScopeKey key, String scopeId) {
    public static final String GLOBAL_SCOPE_ID = "*";

    public AccessScope {
        Objects.requireNonNull(key, "scope key is required");
        if (scopeId == null || scopeId.isBlank()) {
            throw invalidScope(key, scopeId);
        }
        scopeId = scopeId.trim();
        if (key.isGlobal() && !GLOBAL_SCOPE_ID.equals(scopeId)) {
            throw invalidScope(key, scopeId);
        }
        if (!key.isGlobal() && GLOBAL_SCOPE_ID.equals(scopeId)) {
            throw invalidScope(key, scopeId);
        }
    }

    public static AccessScope global() {
        return new AccessScope(ScopeKey.global(), GLOBAL_SCOPE_ID);
    }

    public static AccessScope from(String scopeKey, String scopeId) {
        return new AccessScope(new ScopeKey(scopeKey), scopeId);
    }

    public boolean isGlobal() {
        return key.isGlobal();
    }

    public boolean encompasses(AccessScope target) {
        return encompasses(target, ScopeHierarchy.current());
    }

    public boolean encompasses(AccessScope target, ScopeHierarchy hierarchy) {
        Objects.requireNonNull(target, "target scope is required");
        Objects.requireNonNull(hierarchy, "hierarchy is required");
        if (!hierarchy.isEffective(key.value()) || !hierarchy.isEffective(target.key().value())) {
            return false;
        }
        if (isGlobal()) {
            return true;
        }
        if (equals(target)) {
            return true;
        }
        if (key.equals(target.key())) {
            return false;
        }
        String actorKey = key.value();
        String targetKey = target.key().value();
        return hierarchy.checkAncestorOrSelf(actorKey, targetKey);
    }

    public void requireEncompasses(AccessScope target) {
        requireEncompasses(target, ScopeHierarchy.current());
    }

    public void requireEncompasses(AccessScope target, ScopeHierarchy hierarchy) {
        if (!encompasses(target, hierarchy)) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.AccessScopeNotEncompassed(
                            key.value(), scopeId, target.key().value(), target.scopeId()
                    ),
                    "Access cannot be managed outside the actor scope"
            );
        }
    }

    private static IdentityDomainValidationException invalidScope(ScopeKey key, String scopeId) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidAccessScope(
                        key == null ? "null" : key.value(), String.valueOf(scopeId)
                ),
                "Invalid access scope"
        );
    }
}
