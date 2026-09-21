package com.customer.application.exception;

public sealed interface CustomerServiceError permits CustomerServiceError.NotFound, CustomerServiceError.AlreadyExists {
    record NotFound(String customerId) implements CustomerServiceError {}
    record AlreadyExists(String userId) implements CustomerServiceError {}
}
