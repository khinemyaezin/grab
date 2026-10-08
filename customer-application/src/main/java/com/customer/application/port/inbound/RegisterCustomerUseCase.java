package com.customer.application.port.inbound;

import com.customer.application.model.write.RegisterCustomerCommand;
import com.customer.application.model.write.CustomerResult;

public interface RegisterCustomerUseCase {
    CustomerResult execute(RegisterCustomerCommand command);
}
