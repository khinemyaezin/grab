package com.product.domain.specification;

import com.product.domain.entity.product.Product;
import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.entity.product_variant.ProductVariation;
import com.product.domain.specification.framework.CompositeSpecification;

import java.util.List;

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
