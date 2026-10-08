package com.customer.adapter.persistence.adapter;

import com.customer.adapter.persistence.repository.jpa.CustomerJpaRepository;
import com.customer.application.model.read.CustomerView;
import com.customer.application.port.outbound.CustomerQueryPort;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CustomerQueryAdapter implements CustomerQueryPort {
    private final CustomerJpaRepository customers;
    private final PersistenceExecutor executor;

    @Override
    public Optional<CustomerView> findById(String customerId) {
        return executor.query("Customer", () -> customers.findByUuid(customerId).map(this::toView));
    }

    @Override
    public Optional<CustomerView> findByUserId(String userId) {
        return executor.query("Customer", () -> customers.findByUserId(userId).map(this::toView));
    }

    private CustomerView toView(com.customer.adapter.persistence.entity.CustomerEntity entity) {
        return new CustomerView(
                entity.getUuid(),
                entity.getUserId(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
