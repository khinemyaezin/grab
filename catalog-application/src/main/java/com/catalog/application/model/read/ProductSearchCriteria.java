package com.catalog.application.model.read;

import lombok.Builder;

@Builder
public record ProductSearchCriteria(
    String merchantId,
    String query,
    String variantStatus,
    String categoryId,
    String productStatus,
    Boolean featured,
    String condition,
    boolean storefrontVisible,
    String salesChannelId
)
{ }
