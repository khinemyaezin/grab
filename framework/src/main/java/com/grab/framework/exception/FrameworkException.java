package com.grab.framework.exception;

public class FrameworkException extends DomainException {

    public FrameworkException(FrameworkError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
