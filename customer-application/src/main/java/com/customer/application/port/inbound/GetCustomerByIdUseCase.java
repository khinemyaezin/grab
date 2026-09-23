package com.customer.application.port.inbound;

import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByIdQuery;

public interface GetCustomerByIdUseCase {
    CustomerView execute(GetCustomerByIdQuery query);
}
