package com.customer.application.service;

import com.customer.application.exception.CustomerServiceError;
import com.customer.application.exception.CustomerServiceException;
import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByIdQuery;
import com.customer.application.port.inbound.GetCustomerByIdUseCase;
import com.customer.application.port.outbound.CustomerQueryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetCustomerByIdService implements GetCustomerByIdUseCase {
    private final CustomerQueryPort customers;

    @Override
    public CustomerView execute(GetCustomerByIdQuery query) {
        return customers.findById(query.customerId().getValue())
                .orElseThrow(() -> new CustomerServiceException(
                        new CustomerServiceError.NotFound(query.customerId().getValue()),
                        "Customer not found"
                ));
    }
}
