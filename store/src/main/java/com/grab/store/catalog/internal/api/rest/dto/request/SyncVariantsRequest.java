package com.grab.store.catalog.internal.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.List;

public record SyncVariantsRequest(
        @Valid List<VariantType> variantTypes,
        @Valid List<Variant> variants
) implements Serializable {

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
            @Valid List<Variation> variations
    ) {}

    public record Variation(
            @NotBlank String optionName,
            @NotBlank String optionId,
            @NotBlank String typeId,
            @NotBlank String typeName
    ) {}
}
