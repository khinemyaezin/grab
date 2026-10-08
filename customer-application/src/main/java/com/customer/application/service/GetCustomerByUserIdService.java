package com.customer.application.service;

import com.customer.application.exception.CustomerServiceError;
import com.customer.application.exception.CustomerServiceException;
import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByUserIdQuery;
import com.customer.application.port.inbound.GetCustomerByUserIdUseCase;
import com.customer.application.port.outbound.CustomerQueryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetCustomerByUserIdService implements GetCustomerByUserIdUseCase {
    private final CustomerQueryPort customers;

    @Override
    public CustomerView execute(GetCustomerByUserIdQuery query) {
        return customers.findByUserId(query.userId().getValue())
                .orElseThrow(() -> new CustomerServiceException(
                        new CustomerServiceError.NotFound(query.userId().getValue()),
                        "Customer not found for user"
                ));
    }
}
