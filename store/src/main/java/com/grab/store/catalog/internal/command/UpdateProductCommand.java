package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.List;

public record UpdateProductCommand(
        Id productId,
        String name,
        Id categoryId,
        Id sellerId,
        String sellerType,
        String condition,
        Boolean offerEligible,
        String slug,
        Boolean featured,
        String moderationNote,
        VariantSync variantSync
) implements Command<UpdateProductResult> {

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
