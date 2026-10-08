package com.customer.application.exception;

public class CustomerServiceException extends RuntimeException {
    private final CustomerServiceError error;

    public CustomerServiceException(CustomerServiceError error, String message) {
        super(message);
        this.error = error;
    }

    public CustomerServiceError getError() {
        return error;
    }
}
