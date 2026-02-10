package com.product.infrastructure.specification.jpa;

import java.util.List;

public record ProductSummary(
        String id,
        String name,
        VariantSummary variantSummary
) {

    public record VariantSummary(
            boolean available,
            List<VariantType> types
    ) {}
    public record VariantType(
            String typeId,
            List<VariantOption> options
    ){}

    public record VariantOption(
            String optionId
    ) {}
}
