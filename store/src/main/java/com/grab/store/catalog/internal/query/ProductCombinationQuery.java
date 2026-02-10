package com.grab.store.catalog.internal.query;

import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.cqrs.query.Query;

import java.util.List;

public record ProductCombinationQuery(
        Product product,
        List<VariantType> variantTypes
) implements Query<ProductCombinationResult> {

    public record Product(
            Id id,
            String name,
            Id categoryId,
            List<Variant> variants
    ){}

    public record VariantType(
            String typeId,
       String typeName,
           List<VariantOption> options
    ){}

    public record VariantOption(
            String optionId,
           String optionName
    ) {}

    public record Variant(
            String id,
            String sku,
            String status,
            List<Variation> variations
    ){}

    public record Variation(
            String optionName,
            String optionId,
            String typeId,
            String typeName
    ){}
}
