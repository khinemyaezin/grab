package com.coolstuff.ecommerce.grab.domain.exception;

public class ResourceNotFoundException extends ClientAbstractException {

    public ResourceNotFoundException(Throwable e, ErrorCode error, String... params) {
        super(e, error.getId(), error.getShortMessage(), String.format(error.getMessage(), (Object) params));
    }

    public ResourceNotFoundException(ErrorCode error, String... params) {
        super(error.getId(), error.getShortMessage(), String.format(error.getMessage(), (Object) params));
    }
}
