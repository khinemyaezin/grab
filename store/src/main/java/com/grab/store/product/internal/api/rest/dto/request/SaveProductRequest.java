package com.grab.store.product.internal.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public record SaveProductRequest(
        @Valid @NotNull Product product,
        @Valid List<VariantType> variantTypes
) implements Serializable {

    public record Product(
            String id,
            @NotBlank String name,
            @NotBlank String categoryId,
            @Valid List<Variant> variants
    ) {}

    public record VariantType(
            @NotBlank String typeId,
            @NotBlank String typeName,
            @Valid List<VariantOption> options
    ) {}

    public record VariantOption(
            @NotBlank String optionId,
            @NotBlank String optionName
    ) {}

    public record Variant(
            @NotBlank String id,
            @NotBlank String sku,
            @NotBlank String status,
            @Valid List<Variation> variations
    ) {}

    public record Variation(
            @NotBlank String optionName,
            @NotBlank String optionId,
            @NotBlank String typeId,
            @NotBlank String typeName
    ) {}
}