package com.customer.application.model.write;

import com.customer.application.model.read.CustomerView;
import com.customer.domain.aggregate.Customer;

public record CustomerResult(CustomerView customer) {
    public static CustomerResult from(Customer customer) {
        return new CustomerResult(new CustomerView(
                customer.getId().getValue(),
                customer.getUserIdOptional().map(u -> u.getValue()).orElse(null),
                customer.getEmail(),
                customer.getDisplayName(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        ));
    }
}
