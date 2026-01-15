package com.grab.store.product.internal.command;

import com.grab.store.product.internal.cqrs.command.Command;

import java.util.List;

public record SaveProductCommand(
        Product product,
        List<VariantType> variantTypes
) implements Command<SaveProductResult> {

    public record Product(
            String id,
            String name,
            String categoryId,
            List<Variant> variants
    ) {}

    public record VariantType(
            String typeId,
            String typeName,
            List<VariantOption> options
    ) {}

    public record VariantOption(
            String optionId,
            String optionName
    ) {}

    public record Variant(
            String id,
            String sku,
            String status,
            List<Variation> variations
    ) {}

    public record Variation(
            String optionName,
            String optionId,
            String typeId,
            String typeName
    ) {}
}