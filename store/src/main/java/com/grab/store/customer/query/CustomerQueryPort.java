package com.grab.store.customer.query;

import com.customer.application.model.read.CustomerView;

import java.util.Optional;

public interface CustomerQueryPort {
    Optional<CustomerView> findById(String customerId);
    Optional<CustomerView> findByUserId(String userId);
}
