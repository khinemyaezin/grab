package com.grab.store.product.internal.command;

import com.grab.store.product.internal.cqrs.command.Command;
import jakarta.validation.constraints.NotBlank;

public record DeleteProductCommand(
        @NotBlank String productId
) implements Command<DeleteProductResult> {
}
