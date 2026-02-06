package com.grab.store.product.internal.command;

import com.grab.framework.id.Id;
import com.grab.store.product.internal.cqrs.command.Command;
import jakarta.validation.constraints.NotBlank;

public record DeleteProductCommand(
        Id productId
) implements Command<DeleteProductResult> {
}
