package com.customer.application.service;

import com.customer.application.exception.CustomerServiceError;
import com.customer.application.exception.CustomerServiceException;
import com.customer.application.model.write.AttachUserToCustomerCommand;
import com.customer.application.model.write.CustomerResult;
import com.customer.application.port.inbound.AttachUserToCustomerUseCase;
import com.customer.domain.aggregate.Customer;
import com.customer.domain.port.outbound.CustomerRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class AttachUserToCustomerService implements AttachUserToCustomerUseCase {
    private final CustomerRepository customers;

    @Override
    public CustomerResult execute(AttachUserToCustomerCommand command) {
        Customer customer = customers.findById(command.customerId())
                .orElseThrow(() -> new CustomerServiceException(
                        new CustomerServiceError.NotFound(command.customerId().getValue()),
                        "Customer not found"
                ));
        customer.attachUser(command.userId(), Instant.now());
        return CustomerResult.from(customers.save(customer));
    }
}
