package com.grab.app.platform.seller.internal.adapter.inbound.rest.exception;

import com.grab.framework.exception.DomainException;

public class SharedException extends DomainException {

    public SharedException(SharedError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
