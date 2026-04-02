package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

import java.util.List;

public record CreateProductSetCommand(
        Product product,
        List<VariantType> variantTypes
) implements Command<CreateProductSetResult> {

    public record VariantType(
            String typeId,
            List<VariantOption> options
    ){}

    public record VariantOption(
            String optionId
    ) {}


    public record Product(
            String name,
            Id categoryId,
            Id sellerId,
            String sellerType,
            String condition,
            Boolean offerEligible,
            String slug,
            Boolean featured,
            List<Description> descriptions,
            List<Media> medias,
            List<Variant> variants
    ) {}

    public record Description(
            String name,
            String title,
            String description
    ) {}

    public record Media(
            String type,
            String path
    ) {}

    public record Variant(
            String sku,
            String matrixKey,
            List<Variation> variations
    ) {}

    public record Variation(
            Id optionId,
            Id typeId
    ) {}
} 
