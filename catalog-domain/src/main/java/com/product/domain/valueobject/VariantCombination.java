package com.product.domain.valueobject;

import com.grab.framework.domain.ValueObject;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
public final class VariantCombination extends ValueObject {
    private final List<ProductVariation> variations;

    public VariantCombination(List<ProductVariation> variations) {
        this.variations = variations;
    }
}