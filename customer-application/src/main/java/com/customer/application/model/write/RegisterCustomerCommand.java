package com.customer.application.model.write;

import java.util.UUID;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record RegisterCustomerCommand(
        Id userId,
        String email,
        String displayName,
        UUID sourceEventId
) implements Command<CustomerResult> {

    /**
     * Command not triggered by an outbox delivery; no inbox de-duplication applies.
     */
    public RegisterCustomerCommand(Id userId, String email, String displayName) {
        this(userId, email, displayName, null);
    }
}
