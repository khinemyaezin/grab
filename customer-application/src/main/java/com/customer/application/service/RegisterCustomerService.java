package com.customer.application.service;

import com.customer.application.exception.CustomerServiceError;
import com.customer.application.exception.CustomerServiceException;
import com.customer.application.model.write.CustomerResult;
import com.customer.application.model.write.RegisterCustomerCommand;
import com.customer.application.port.inbound.RegisterCustomerUseCase;
import com.customer.domain.aggregate.Customer;
import com.customer.domain.port.outbound.CustomerRepository;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class RegisterCustomerService implements RegisterCustomerUseCase {
    private final CustomerRepository customers;
    private final IdGenerator ids;

    @Override
    public CustomerResult execute(RegisterCustomerCommand command) {
        if (customers.findByUserId(command.userId()).isPresent()) {
            throw new CustomerServiceException(
                    new CustomerServiceError.AlreadyExists(command.userId().getValue()),
                    "Customer already exists for user"
            );
        }
        Instant now = Instant.now();
        Customer customer = Customer.register(
                ids.generateId(),
                command.userId(),
                command.email(),
                command.displayName(),
                now
        );
        return CustomerResult.from(customers.save(customer));
    }
}
