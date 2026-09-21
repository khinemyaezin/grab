package com.identity.adapter.persistence.exception;

import com.grab.framework.exception.DomainException;

public class IdentityInfraException extends DomainException {
    public IdentityInfraException(IdentityInfraError error, String message, Throwable cause) {
        super(error, message, cause);
    }
}
