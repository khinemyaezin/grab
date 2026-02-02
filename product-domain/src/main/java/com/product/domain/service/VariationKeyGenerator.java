package com.product.domain.service;

import com.product.domain.valueobject.ProductVariation;

import java.util.List;

public interface VariationKeyGenerator {
    String generateVariationKey(List<ProductVariation> variations);
}
