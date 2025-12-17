package com.grab.store.product.internal.query;

import com.grab.framework.id.Id;

import java.util.List;

public record ProductCombinationResult(
        List<VariantType> requestedTypes,
        List<VariantCombination> combinations,
        int totalCount
) {
    public record VariantType(
            Id typeId,
            String typeName,
            List<VariantOption> options
    ) {}

    public record VariantOption(
            Id optionId,
            String optionName
    ){}

    public record VariantCombination(
            Id combinationId,
            List<Variation> variations
    ) {}

    public record Variation(
            Id optionId,
            String optionName,
            Id typeId,
            String typeName
    ) {}
}
