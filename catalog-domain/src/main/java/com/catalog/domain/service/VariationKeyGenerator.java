package com.catalog.domain.service;

import com.catalog.domain.valueobject.ProductVariation;

import java.util.List;

public interface VariationKeyGenerator {
    String generateVariationKey(List<ProductVariation> variations);
}
