package com.grab.framework.exception;

import com.grab.framework.service.MessageSource;

public abstract class DomainException extends RuntimeException {
    protected final MessageSource messageSource;

    public DomainException(MessageSource messageSource, String defaultMessage) {
        super(defaultMessage);
        this.messageSource = messageSource;
    }
}
