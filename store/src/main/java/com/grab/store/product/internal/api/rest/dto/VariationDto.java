package com.grab.store.product.internal.api.rest.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record VariationDto(
        String optionId,
        @NotEmpty String optionName,
        String typeId,
        @NotEmpty String typeName
) implements Serializable  {
}
