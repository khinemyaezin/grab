package com.catalog.application.model.read;

public record GetCatalogProductFactsResult(
        String productId,
        String merchantId,
        GetProductResult product
) {
}
