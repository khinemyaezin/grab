package com.grab.store.catalog.internal.api.rest.dto.request;

import com.grab.framework.id.Id;

import java.io.Serializable;
import java.util.List;

public record UpdateProductRequest(
        String name,
        String categoryId,
        String sellerId,
        String sellerType,
        String condition,
        Boolean offerEligible,
        String slug,
        Boolean featured,
        String moderationNote,
        VariantSync variantSync
) implements Serializable {

    public enum VariantSyncIntent {
        LEAVE_AS_IS,
        FULL_SYNC,
        COLLAPSE_TO_STANDALONE
    }

    public record VariantSync(
            VariantSyncIntent intent,
            List<Variant> overrides,
            List<VariantType> variantTypes
    ) {}

    public record Variant(
            String sku,
            String matrixKey,
            List<Variation> variations
    ) {}

    public record Variation(
            Id typeId,
            Id optionId
    ){}

    public record VariantType(
            Id typeId,
            List<VariantOption> options
    ) {}

    public record VariantOption(
            Id optionId,
            String optionName
    ) {}
}
