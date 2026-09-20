package com.identity.application.exception;

import com.grab.framework.exception.DomainException;

public class IdentityServiceException extends DomainException {
    public IdentityServiceException(IdentityServiceError error, String message) {
        super(error, message);
    }
}
