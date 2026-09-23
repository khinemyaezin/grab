package com.customer.application.port.inbound;

import com.customer.application.model.write.AttachUserToCustomerCommand;
import com.customer.application.model.write.CustomerResult;

public interface AttachUserToCustomerUseCase {
    CustomerResult execute(AttachUserToCustomerCommand command);
}
