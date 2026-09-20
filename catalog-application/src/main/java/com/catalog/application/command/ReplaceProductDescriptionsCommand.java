package com.catalog.application.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.List;

public record ReplaceProductDescriptionsCommand(
        Id merchantId,
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
