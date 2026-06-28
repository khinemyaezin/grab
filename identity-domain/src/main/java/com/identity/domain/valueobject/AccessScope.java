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
        Objects.requireNonNull(target, "target scope is required");
        return isGlobal() || equals(target);
    }

    public void requireEncompasses(AccessScope target) {
        if (!encompasses(target)) {
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
