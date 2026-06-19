package com.identity.domain.valueobject;

import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;

public record HashedPassword(String hash) {
    public HashedPassword {
        if (hash == null || hash.isBlank()) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidPasswordHash(),
                    "Password hash is required"
            );
        }
    }
}
