package com.customer.application.port.inbound;

import com.customer.application.model.write.CreateGuestCustomerCommand;
import com.customer.application.model.write.CustomerResult;

public interface CreateGuestCustomerUseCase {
    CustomerResult execute(CreateGuestCustomerCommand command);
}
