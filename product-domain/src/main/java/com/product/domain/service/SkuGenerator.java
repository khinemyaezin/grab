package com.product.domain.service;

import com.grab.framework.id.Id;
import com.product.domain.valueobject.ProductVariation;
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
