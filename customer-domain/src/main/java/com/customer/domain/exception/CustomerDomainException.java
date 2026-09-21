package com.customer.domain.exception;

public class CustomerDomainException extends RuntimeException {
    private final CustomerDomainError error;

    public CustomerDomainException(CustomerDomainError error, String message) {
        super(message);
        this.error = error;
    }

    public CustomerDomainError getError() {
        return error;
    }
}
