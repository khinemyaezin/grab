package com.catalog.domain.service;

import com.catalog.domain.valueobject.ProductVariation;

import java.util.List;

public interface MatrixKeyGenerator {
    String generateKey(List<ProductVariation> variations);
}
