package com.inventory.domain.exception;

import com.grab.framework.exception.DomainException;

public class InventoryDomainValidationException extends DomainException {
    public InventoryDomainValidationException(InventoryDomainError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
