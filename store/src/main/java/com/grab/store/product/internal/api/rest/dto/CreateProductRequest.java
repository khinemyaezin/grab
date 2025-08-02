package com.grab.store.product.internal.api.rest.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record CreateProductRequest(
        @NotEmpty
        @NotNull
        String name,
        @NotEmpty
        @NotNull
        String categoryId,
        List<Variant> variants) implements Serializable {

        @Builder
        public record Variant(
                @NotEmpty String sku,
                List<VariationDto> variations) {}
}
