package com.product.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;

public class UniqueProductVariantSpec extends CompositeSpecification<Product> {
    private final ProductVariant productVariant;

    public UniqueProductVariantSpec(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    @Override
    public boolean isSatisfiedBy(Product product) {
        return product.getVariants().stream()
                .noneMatch(variant -> variant.getVariations().equals(productVariant.getVariations())
                        || variant.getSku().equals( productVariant.getSku())
                        || variant.getId().equals( productVariant.getId()));
    }
}
