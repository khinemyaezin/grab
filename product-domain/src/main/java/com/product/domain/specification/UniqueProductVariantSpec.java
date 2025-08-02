package com.product.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;

import java.util.Objects;

public class UniqueProductVariantSpec extends CompositeSpecification<Product> {
    private final ProductVariant productVariant;

    public UniqueProductVariantSpec(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    @Override
    public boolean isSatisfiedBy(Product product) {
        return product.getVariants().stream()
                .noneMatch(variant -> Objects.equals(variant, this.productVariant));
    }
}
