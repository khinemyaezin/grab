package com.customer.domain.exception;

public sealed interface CustomerDomainError permits CustomerDomainError.NotFound, CustomerDomainError.AlreadyRegistered,
        CustomerDomainError.UserAlreadyAttached, CustomerDomainError.InvalidState {
    record NotFound(String customerId) implements CustomerDomainError {
    }
    record AlreadyRegistered(String userId) implements CustomerDomainError {
    }
    record UserAlreadyAttached(String customerId) implements CustomerDomainError {
    }
    record InvalidState(String customerId, String status) implements CustomerDomainError {
    }
}
