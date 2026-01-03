package com.grab.store.product.internal.query;

import com.grab.store.product.internal.cqrs.query.Query;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BuildProductQuery (
        @Valid
        @Nonnull
        Product product,
        List<VariantType> variantTypes
) implements Query<BuildProductResult> {

    public record Product(
            String id,
            @NotBlank
            String name,
            @NotBlank
            String categoryId,
            List<Variant> variants
    ){}

    public record VariantType(
            String typeId,
            @NotBlank String typeName,
            @NotEmpty @Valid List<VariantOption> options
    ){}

    public record VariantOption(
            String optionId,
            @NotBlank String optionName
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
