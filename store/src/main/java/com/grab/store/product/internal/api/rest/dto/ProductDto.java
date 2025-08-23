package com.grab.store.product.internal.api.rest.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ProductDto(
        String id,
        String name,
        String categoryId,
        List<VariantDto> variants) implements Serializable {
}
