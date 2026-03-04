package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

public record UpdateProductCommand(
        Id productId,
        String name,
        Id categoryId,
        String slug,
        Boolean featured
) implements Command<UpdateProductResult> {}
