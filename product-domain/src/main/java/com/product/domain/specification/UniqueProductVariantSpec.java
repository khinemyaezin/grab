package com.product.domain.specification;

import com.product.domain.entity.product.Product;
import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.specification.framework.CompositeSpecification;

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
