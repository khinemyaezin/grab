package com.grab.store.customer.internal.adapter;

import com.customer.application.model.read.CustomerView;
import com.customer.application.port.outbound.CustomerQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class StoreCustomerQueryAdapter implements CustomerQueryPort {
    private final CustomerQueryPort delegate;

    @Override
    public Optional<CustomerView> findById(String customerId) {
        return delegate.findById(customerId);
    }

    @Override
    public Optional<CustomerView> findByUserId(String userId) {
        return delegate.findByUserId(userId);
    }
}
