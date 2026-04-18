package com.grab.store.catalog.internal.api.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

public record VariationMatrixRequest(
    @Valid
    List<Variant> variants,
    @NotEmpty
    @Valid
    List<VariantType> variantTypes

) implements Serializable{
    @Schema(name = "MatrixVariantType")
    public record VariantType(
            @NotBlank String typeId,
            @NotEmpty @Valid List<VariantOption> options
    ){}

    @Schema(name = "MatrixVariantOption")
    public record VariantOption(
            @NotBlank String optionId
    ) {}

    @Schema(name = "MatrixVariant")
    public record Variant(
            @NotBlank String matrixKey,
            @NotBlank String sku,
            @Valid List<Variation> variations
    ){}

    @Schema(name = "MatrixVariation")
    public record Variation(
            @NotBlank String optionId,
            @NotBlank String typeId
    ){}

}
