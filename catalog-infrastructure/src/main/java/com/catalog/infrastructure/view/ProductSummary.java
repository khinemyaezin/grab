package com.catalog.infrastructure.view;

import java.util.List;

public record ProductSummary(
        String id,
        String name,
        String sellerId,
        String sellerType,
        boolean offerEligible,
        String status,
        String slug,
        boolean featured,
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
