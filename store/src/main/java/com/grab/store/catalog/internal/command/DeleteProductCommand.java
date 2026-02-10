package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.cqrs.command.Command;

public record DeleteProductCommand(
        Id productId
) implements Command<DeleteProductResult> {
}
