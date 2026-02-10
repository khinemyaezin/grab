package com.catalog.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.catalog.domain.aggregate.ProductVariant;

import java.util.List;

public class UniqueSkuSpec extends CompositeSpecification<ProductVariant> {
    private final List<String> sku;

    public UniqueSkuSpec(List<String> sku) {
        this.sku = sku;
    }

    @Override
    public boolean isSatisfiedBy(ProductVariant prod) {
        return sku.stream()
                .noneMatch(sku-> sku.equalsIgnoreCase(prod.getSku()));
    }
}
