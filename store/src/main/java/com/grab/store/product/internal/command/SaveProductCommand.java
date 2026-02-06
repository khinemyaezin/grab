package com.grab.store.product.internal.command;

import com.grab.framework.id.Id;
import com.grab.store.product.internal.cqrs.command.Command;

import java.util.List;

public record SaveProductCommand(
        Product product
) implements Command<SaveProductResult> {

    public record Product(
            Id id,
            String name,
            Id categoryId,
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