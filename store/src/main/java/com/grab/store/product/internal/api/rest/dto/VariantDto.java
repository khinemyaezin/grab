package com.grab.store.product.internal.api.rest.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record VariantDto(
        String id,
        String sku,
        List<VariationDto> variations
) implements Serializable {
}
