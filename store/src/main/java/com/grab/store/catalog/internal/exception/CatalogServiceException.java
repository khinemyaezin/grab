package com.grab.store.catalog.internal.exception;

import com.grab.framework.exception.DomainException;

public class CatalogServiceException extends DomainException {

    public CatalogServiceException(CatalogServiceError error) {
        super(error, error.code());
    }

    public CatalogServiceException(CatalogServiceError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
