package com.catalog.domain.service;

import com.catalog.domain.valueobject.ProductVariation;
import lombok.Builder;

import java.util.List;
import java.util.Objects;

public interface SkuGenerator {
    String generate(Context context);

    @Builder
    record Context(
            String productName,
            List<ProductVariation> orderedVariations
    ) {
        public Context {
            Objects.requireNonNull(orderedVariations, "orderedVariations is required");
        }
    }
}
