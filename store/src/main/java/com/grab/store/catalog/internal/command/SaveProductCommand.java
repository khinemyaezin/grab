package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

import java.util.List;

public record SaveProductCommand(
        Product product
) implements Command<SaveProductResult> {

    public record Product(
            Id id,
            String name,
            Id categoryId,
            String slug,
            Boolean featured,
            List<Variant> variants
    ) {}

    public record Variant(
            Id id,
            String sku,
            String status,
            List<Variation> variations
    ) {}

    public record Variation(
            String optionName,
            Id optionId,
            Id typeId,
            String typeName
    ) {}
}