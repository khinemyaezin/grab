package com.grab.store.product.internal.query;

import java.util.List;

public record GetAllProductsResult(List<Product> products) {

    public record Product(
            String id,
            String name,
            String categoryId,
            List<Variant> variants
    ) {}

    public record Variant(
            String id,
            String sku,
            String status,
            List<Variation> variations
    ) {}

    public record Variation(
            String optionName,
            String optionId,
            String typeId,
            String typeName
    ) {}
}
