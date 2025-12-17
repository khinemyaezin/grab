package com.grab.store.product.internal.query;

import com.grab.framework.id.Id;
import com.grab.store.product.internal.cqrs.query.Query;

import java.util.List;

public record ProductCombinationQuery(
        List<VariantType> desiredVariantTypes
) implements Query<ProductCombinationResult> {

    public record VariantType(
            Id typeId,
            String typeName,
            List<VariantOption> options
    ) {}

    public record VariantOption(
            Id optionId,
            String optionName
    ) {}
}
