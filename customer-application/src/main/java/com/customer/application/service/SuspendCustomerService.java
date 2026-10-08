package com.customer.application.service;

import com.customer.application.exception.CustomerServiceError;
import com.customer.application.exception.CustomerServiceException;
import com.customer.application.model.write.CustomerResult;
import com.customer.application.model.write.SuspendCustomerCommand;
import com.customer.application.port.inbound.SuspendCustomerUseCase;
import com.customer.domain.aggregate.Customer;
import com.customer.domain.port.outbound.CustomerRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class SuspendCustomerService implements SuspendCustomerUseCase {
    private final CustomerRepository customers;

    @Override
    public CustomerResult execute(SuspendCustomerCommand command) {
        Customer customer = customers.findById(command.customerId())
                .orElseThrow(() -> new CustomerServiceException(
                        new CustomerServiceError.NotFound(command.customerId().getValue()),
                        "Customer not found"
                ));
        customer.suspend(Instant.now());
        return CustomerResult.from(customers.save(customer));
    }
}
