package com.catalog.application.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

public record DeleteProductCommand(
        Id merchantId,
        Id productId
) implements Command<DeleteProductResult> {
}
