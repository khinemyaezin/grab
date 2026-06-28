package com.identity.domain.valueobject;

import com.identity.domain.enums.AccessScopeType;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;

import java.util.Locale;
import java.util.Objects;

public record AccessScope(AccessScopeType type, String scopeId) {
    public static final String GLOBAL_SCOPE_ID = "*";

    public AccessScope {
        Objects.requireNonNull(type, "scope type is required");
        if (scopeId == null || scopeId.isBlank()) {
            throw invalidScope(type, scopeId);
        }
        scopeId = scopeId.trim();
        if (type == AccessScopeType.GLOBAL && !GLOBAL_SCOPE_ID.equals(scopeId)) {
            throw invalidScope(type, scopeId);
        }
        if (type != AccessScopeType.GLOBAL && GLOBAL_SCOPE_ID.equals(scopeId)) {
            throw invalidScope(type, scopeId);
        }
    }

    public static AccessScope global() {
        return new AccessScope(AccessScopeType.GLOBAL, GLOBAL_SCOPE_ID);
    }

    public static AccessScope from(String scopeType, String scopeId) {
        try {
            return new AccessScope(
                    AccessScopeType.valueOf(scopeType.trim().toUpperCase(Locale.ROOT)),
                    scopeId
            );
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw invalidScope(null, scopeId);
        }
    }

    public boolean isGlobal() {
        return type == AccessScopeType.GLOBAL;
    }

    public boolean encompasses(AccessScope target) {
        Objects.requireNonNull(target, "target scope is required");
        return isGlobal() || equals(target);
    }

    public void requireEncompasses(AccessScope target) {
        if (!encompasses(target)) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.AccessScopeNotEncompassed(
                            type.name(), scopeId, target.type().name(), target.scopeId()
                    ),
                    "Access cannot be managed outside the actor scope"
            );
        }
    }

    private static IdentityDomainValidationException invalidScope(AccessScopeType type, String scopeId) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidAccessScope(String.valueOf(type), String.valueOf(scopeId)),
                "Invalid access scope"
        );
    }
}
