package com.customer.adapter.persistence.exception;

import com.grab.framework.exception.DomainException;

public class CustomerInfraException extends DomainException {
    public CustomerInfraException(CustomerInfraError error, String message, Throwable cause) {
        super(error, message, cause);
    }
}
