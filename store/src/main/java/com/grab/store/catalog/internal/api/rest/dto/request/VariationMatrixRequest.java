package com.grab.store.catalog.internal.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

public record VariationMatrixRequest(
    @NotEmpty
    @Valid
    List<Variant> variants,
    @NotEmpty
    @Valid
    List<VariantType> variantTypes

) implements Serializable{
    public record VariantType(
            @NotBlank String typeId,
            @NotEmpty @Valid List<VariantOption> options
    ){}

    public record VariantOption(
            @NotBlank String optionId
    ) {}

    public record Variant(
            @NotBlank String matrixKey,
            @Valid List<Variation> variations
    ){}

    public record Variation(
            @NotBlank String optionId,
            @NotBlank String typeId
    ){}

}
