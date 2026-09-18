package com.saleschannel.domain.exception;

import com.grab.framework.exception.DomainException;

public class SalesChannelDomainException extends DomainException {
    public SalesChannelDomainException(SalesChannelDomainError error, String message) {
        super(error, message);
    }
}
