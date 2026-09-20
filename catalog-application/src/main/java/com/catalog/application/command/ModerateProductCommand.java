package com.catalog.application.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ModerateProductCommand(
        Id productId,
        String action,
        String reason
) implements Command<ModerateProductResult> {
}
