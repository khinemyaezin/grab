package com.product.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;

public class VariationSizeSpec extends CompositeSpecification<Product> {
    private final ProductVariant productVariant;

    public VariationSizeSpec(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    @Override
    public boolean isSatisfiedBy(Product product) {
        if (product.getVariants().isEmpty()) {
            return true; // No variants, so the specification is satisfied.
        }
        return true;
    }
}
