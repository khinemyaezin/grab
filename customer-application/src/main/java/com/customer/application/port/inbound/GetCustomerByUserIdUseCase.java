package com.customer.application.port.inbound;

import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByUserIdQuery;

public interface GetCustomerByUserIdUseCase {
    CustomerView execute(GetCustomerByUserIdQuery query);
}
