package com.cart.domain.exception;

import com.grab.framework.exception.DomainException;

public class CartDomainException extends DomainException {
    public CartDomainException(CartDomainError error, String message) {
        super(error, message);
    }
}
