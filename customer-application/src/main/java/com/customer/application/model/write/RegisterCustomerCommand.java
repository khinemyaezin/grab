package com.customer.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record RegisterCustomerCommand(
        Id userId,
        String email,
        String displayName
) implements Command<CustomerResult> {
}
