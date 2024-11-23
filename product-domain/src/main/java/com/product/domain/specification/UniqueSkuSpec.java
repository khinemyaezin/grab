package com.product.domain.specification;

import com.product.domain.entity.product.Product;
import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.specification.framework.CompositeSpecification;

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
