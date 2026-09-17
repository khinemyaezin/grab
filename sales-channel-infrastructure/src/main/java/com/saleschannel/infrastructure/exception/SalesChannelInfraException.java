package com.saleschannel.infrastructure.exception;

import com.grab.framework.exception.DomainException;

public class SalesChannelInfraException extends DomainException {
    public SalesChannelInfraException(SalesChannelInfraError error, String message, Throwable cause) {
        super(error, message, cause);
    }
}
