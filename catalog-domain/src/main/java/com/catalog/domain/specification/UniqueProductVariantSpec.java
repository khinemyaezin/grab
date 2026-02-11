package com.catalog.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;

import java.util.Objects;

public class UniqueProductVariantSpec extends CompositeSpecification<Product> {
    private final ProductVariant productVariant;
    private int ignoredVariantIndex = -1;

    public UniqueProductVariantSpec(ProductVariant productVariant, int ignoredVariantIndex) {
        this.productVariant = productVariant;
        this.ignoredVariantIndex = ignoredVariantIndex;
    }

    public UniqueProductVariantSpec(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    @Override
    public boolean isSatisfiedBy(Product product) {
        var variants = product.getVariants();
        for (int i = 0; i < variants.size(); i++) {
            if (ignoredVariantIndex >= 0 && i == ignoredVariantIndex) {
                continue;
            }
            if (Objects.equals(variants.get(i), this.productVariant)) {
                return false;
            }
        }
        return true;
    }
}
