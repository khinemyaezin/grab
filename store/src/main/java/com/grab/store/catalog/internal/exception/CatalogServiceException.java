package com.grab.store.catalog.internal.exception;

import com.grab.framework.exception.DomainException;
import com.grab.framework.exception.MessageSource;

public class CatalogServiceException extends DomainException {

    public CatalogServiceException(MessageSource error) {
        super(error, error.code());
    }

    public CatalogServiceException(CatalogServiceError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
