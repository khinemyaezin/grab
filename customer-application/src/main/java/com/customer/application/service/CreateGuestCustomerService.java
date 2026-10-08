package com.customer.application.service;

import com.customer.application.model.write.CreateGuestCustomerCommand;
import com.customer.application.model.write.CustomerResult;
import com.customer.application.port.inbound.CreateGuestCustomerUseCase;
import com.customer.domain.aggregate.Customer;
import com.customer.domain.port.outbound.CustomerRepository;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class CreateGuestCustomerService implements CreateGuestCustomerUseCase {
    private final CustomerRepository customers;
    private final IdGenerator ids;

    @Override
    public CustomerResult execute(CreateGuestCustomerCommand command) {
        Instant now = Instant.now();
        Customer customer = Customer.createGuest(ids.generateId(), command.email(), command.displayName(), now);
        return CustomerResult.from(customers.save(customer));
    }
}
