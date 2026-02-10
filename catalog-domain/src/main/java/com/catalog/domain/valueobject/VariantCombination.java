package com.catalog.domain.valueobject;

import com.grab.framework.domain.ValueObject;
import lombok.Getter;

import java.util.List;

@Getter
public final class VariantCombination extends ValueObject {
    private final List<ProductVariation> variations;

    public VariantCombination(List<ProductVariation> variations) {
        this.variations = variations;
    }
}