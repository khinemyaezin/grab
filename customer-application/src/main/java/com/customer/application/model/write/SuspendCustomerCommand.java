package com.customer.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record SuspendCustomerCommand(Id customerId) implements Command<CustomerResult> {
}
