package com.catalog.infrastructure.exception;

import com.grab.framework.exception.DomainException;

public class CatalogInfraException extends DomainException {

    public CatalogInfraException(CatalogInfraError error, String defaultMessage) {
        super(error, defaultMessage);
    }

    public CatalogInfraException(CatalogInfraError error, String defaultMessage, Throwable cause) {
        super(error, defaultMessage, cause);
    }
}
