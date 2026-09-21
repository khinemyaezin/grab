package com.grab.store.storefrontquery.internal.exception;

import com.grab.framework.exception.DomainException;

public class StorefrontQueryServiceException extends DomainException {
    public StorefrontQueryServiceException(StorefrontQueryServiceError error, String message) {
        super(error, message);
    }
}
