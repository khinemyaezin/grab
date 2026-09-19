package com.grab.store.catalog.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ApplyProductStatusCommand(
        Id merchantId,
        Id productId,
        String status
) implements Command<ApplyProductStatusResult> {
}
