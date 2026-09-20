package com.cart.infrastructure.exception;

import com.grab.framework.exception.DomainException;

public class CartInfraException extends DomainException {
    public CartInfraException(CartInfraError error, String message, Throwable cause) {
        super(error, message, cause);
    }
}
