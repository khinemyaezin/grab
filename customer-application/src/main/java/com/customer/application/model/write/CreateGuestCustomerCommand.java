package com.customer.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record CreateGuestCustomerCommand(
        String email,
        String displayName
) implements Command<CustomerResult> {
}
