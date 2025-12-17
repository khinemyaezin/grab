package com.grab.store.product.internal.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ProductCombinationRequest(
        @NotEmpty
        @Valid
        List<VariantTypeRequest> variantTypes
) implements Serializable {

    @Builder
    public record VariantTypeRequest(
            String typeId,
            @NotBlank String typeName,
            @NotEmpty @Valid List<VariantOptionRequest> options
    ) implements Serializable {}

    @Builder
    public record VariantOptionRequest(
            String optionId,
            @NotBlank String optionName
    ) implements Serializable {}
}