package com.identity.domain.exception;

import com.grab.framework.exception.DomainException;

public class IdentityDomainValidationException extends DomainException {
    public IdentityDomainValidationException(IdentityDomainError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
