package com.grab.framework.exception;

public abstract class DomainException extends RuntimeException {
    protected final MessageSource messageSource;

    public DomainException(MessageSource messageSource, String defaultMessage) {
        super(defaultMessage);
        this.messageSource = messageSource;
    }

    public DomainException(MessageSource messageSource, String defaultMessage, Throwable cause) {
        super(defaultMessage, cause);
        this.messageSource = messageSource;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }
}
