package com.catalog.domain.service.dto;

import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.Id;

import java.util.List;

public record ProductVariantSelection (
        Id id,
       // String sku,
        List<ProductVariation> variations,
        ProductVariantStatus status
) {
}
