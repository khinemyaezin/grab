package com.catalog.infrastructure.specification.jpa;

import com.catalog.domain.valueobject.ProductStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductSearchCriteria(
    String productName,
    String sku,
    String variantStatus,
    String categoryId,
    String sellerId,
    String sellerType,
    Boolean offerEligible,
    ProductStatus productStatus,
    Boolean feature,
    List<String> variations){
}
