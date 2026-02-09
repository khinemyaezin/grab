package com.product.infrastructure.specification.jpa;

import java.util.List;
import java.util.Map;

public record ProductSummary(
        String id,
        String name,
        VariantSummary variants
) {

    public record VariantSummary(
            boolean available,
            Map<String, List<String>> options
    ) {}
}
