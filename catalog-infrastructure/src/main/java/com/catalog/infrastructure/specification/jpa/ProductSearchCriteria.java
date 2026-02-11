package com.catalog.infrastructure.specification.jpa;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductSearchCriteria(
    String productName,
    String sku,
    String variantStatus,
    String categoryId,
    String productStatus,
    Boolean feature,
    List<String> variations){
}
