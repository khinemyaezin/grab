package com.catalog.infrastructure.specification.jpa;

import lombok.Builder;

@Builder
public record ProductSearchCriteria(
    String merchantId,
    String query,
    String variantStatus,
    String categoryId,
    String productStatus
)
{ }
