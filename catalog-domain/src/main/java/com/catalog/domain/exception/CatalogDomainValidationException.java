package com.catalog.domain.exception;

import com.grab.framework.exception.DomainException;

public class CatalogDomainValidationException extends DomainException {
    public CatalogDomainValidationException(CatalogDomainError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
