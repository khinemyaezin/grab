package com.product.infrastructure.specification.jpa;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductSearchCriteria(
    String productName,
    String sku,
    String variantStatus,
    List<String> variations){
}
