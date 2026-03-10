package com.inventory.infrastructure.exception;

import com.grab.framework.exception.DomainException;

public class InventoryInfraException extends DomainException {

    public InventoryInfraException(InventoryInfraError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
