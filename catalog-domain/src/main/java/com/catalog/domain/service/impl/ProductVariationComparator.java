package com.catalog.domain.service.impl;

import com.catalog.domain.valueobject.ProductVariation;

import java.util.Comparator;

public final class ProductVariationComparator implements Comparator<ProductVariation> {

    private static final Comparator<String> NULL_SAFE_COMPARATOR =
            Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER);

    @Override
    public int compare(ProductVariation v1, ProductVariation v2) {
        String value1 = v1.getTypeId() != null ? v1.getTypeId().getValue() : null;
        String value2 = v2.getTypeId() != null ? v2.getTypeId().getValue() : null;
        return NULL_SAFE_COMPARATOR.compare(value1, value2);
    }
}