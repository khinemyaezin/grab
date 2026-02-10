package com.product.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;

public class CombinationSpec extends CompositeSpecification<Product> {
   private final ProductVariant productVariant;

    public CombinationSpec(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    @Override
    public boolean isSatisfiedBy(Product prod) {
        return prod.getVariants().stream()
                .noneMatch( v-> v.getVariations().equals(this.productVariant.getVariations()));
    }
}
