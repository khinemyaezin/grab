package com.product.domain.specification;

import com.product.domain.entity.product.Product;
import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.entity.product_variant.ProductVariation;
import com.product.domain.specification.framework.CompositeSpecification;

import java.util.List;

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
        return productVariant.getVariations().size() == product.getVariantTypes().size();
    }
}
