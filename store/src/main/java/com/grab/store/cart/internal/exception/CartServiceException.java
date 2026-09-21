package com.grab.store.cart.internal.exception;

import com.grab.framework.exception.DomainException;

public class CartServiceException extends DomainException {
    public CartServiceException(CartServiceError error, String message) {
        super(error, message);
    }
}
