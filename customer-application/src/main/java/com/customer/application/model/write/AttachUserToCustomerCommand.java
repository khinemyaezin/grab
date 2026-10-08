package com.customer.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record AttachUserToCustomerCommand(
        Id customerId,
        Id userId
) implements Command<CustomerResult> {
}
