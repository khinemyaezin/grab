package com.grab.store.product.internal.api.rest.dto.response;

import java.io.Serializable;
import java.util.List;

/**
 * HATEOAS-ready response DTO for variant combinations.
 */
public record ProductCombinationResponse(
        List<RequestedVariantType> requestedVariantTypes,
        List<CombinationResponse> combinations,
        int totalCombinations
) implements Serializable {
    public record RequestedVariantType(
            String typeId,
            String typeName,
            List<RequestedVariantOption> options
    ) {}

    public record RequestedVariantOption(
            String optionId,
            String optionName
    ) {}

    public record CombinationResponse(
            String id,
            List<VariationResponse> variations
    ) {}

    public record VariationResponse(
            String optionId,
            String optionName,
            String typeId,
            String typeName
    ) {}
}
