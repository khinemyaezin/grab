package com.catalog.application.model.write;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

import java.util.List;

public record UpdateProductCommand(
        Id merchantId,
        Id productId,
        String name,
        Id categoryId,
        String condition,
        String slug,
        VariantSync variantSync,
        String status
) implements Command<UpdateProductResult> {

    public UpdateProductCommand(
            Id merchantId,
            Id productId,
            String name,
            Id categoryId,
            String condition,
            String slug,
            VariantSync variantSync
    ) {
        this(merchantId, productId, name, categoryId, condition, slug, variantSync, null);
    }

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
            List<Variation> variations,
            Boolean manageInventory
    ) {
        public Variant(String sku, String matrixKey, List<Variation> variations) {
            this(sku, matrixKey, variations, null);
        }
    }

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
