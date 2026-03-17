package com.grab.store.catalog.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.List;

public record PatchProductDescriptionsCommand(
        Id productId,
        List<Description> descriptions
) implements Command<ProductDescriptionsResult> {
    public record Description(
            Id id,
            String name,
            String title,
            String description
    ) {}
}
