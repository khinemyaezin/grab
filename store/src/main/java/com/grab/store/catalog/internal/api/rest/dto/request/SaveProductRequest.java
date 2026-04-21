package com.grab.store.catalog.internal.api.rest.dto.request;

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
            @NotBlank String name,
            @NotBlank String categoryId,
            String condition,
            String slug,
            @Valid List<Variant> variants
    ) {}

    public record Description(
            @NotBlank String name,
            String title,
            @NotBlank String description
    ) {}

    public record Media(
            String type,
            @NotBlank String path
    ) {}

    public record VariantType(
            @NotBlank String typeId,
            @Valid List<VariantOption> options
    ) {}

    public record VariantOption(
            @NotBlank String optionId
    ) {}

    public record Variant(
            @NotBlank String sku,
            @Valid List<Variation> variations
    ) {}

    public record Variation(
            @NotBlank String optionId,
            @NotBlank String typeId
    ) {}
}
