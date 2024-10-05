package com.coolstuff.ecommerce.grab.domain.exception;

public class InternalServerException extends InternalAbstractException {

    public InternalServerException(Throwable e, ErrorCode error, String... params) {
        super(e, error.getId(), error.getShortMessage(), String.format(error.getMessage(), (Object) params));
    }

    public InternalServerException(ErrorCode error, String... params) {
        super(error.getId(), error.getShortMessage(), String.format(error.getMessage(), (Object) params));
    }
}
