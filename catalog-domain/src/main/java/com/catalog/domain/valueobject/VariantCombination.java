package com.catalog.domain.valueobject;

import java.util.List;

public record VariantCombination (
        List<ProductVariation> variations
){}