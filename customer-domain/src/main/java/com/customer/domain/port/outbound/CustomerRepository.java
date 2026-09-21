package com.customer.domain.port.outbound;

import com.customer.domain.aggregate.Customer;
import com.grab.framework.id.Id;

import java.util.Optional;

public interface CustomerRepository {
    Optional<Customer> findById(Id id);
    Optional<Customer> findByUserId(Id userId);
    Optional<Customer> findByEmail(String email);
    Customer save(Customer customer);
}
