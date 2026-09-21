package com.customer.application.port.inbound;

import com.customer.application.model.write.SuspendCustomerCommand;
import com.customer.application.model.write.CustomerResult;

public interface SuspendCustomerUseCase {
    CustomerResult execute(SuspendCustomerCommand command);
}
