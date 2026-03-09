package com.grab.store.shared.exception;

import com.grab.framework.exception.DomainException;

public class SharedException extends DomainException {

    public SharedException(SharedError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
