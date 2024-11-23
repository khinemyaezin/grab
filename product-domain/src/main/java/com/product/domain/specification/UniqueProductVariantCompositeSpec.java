package com.product.domain.specification;

import com.product.domain.entity.product.Product;
import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.specification.framework.CompositeSpecification;

public class UniqueProductVariantCompositeSpec extends CompositeSpecification<Product> {

    private final CompositeSpecification<Product> combinationSpec;
    private final CompositeSpecification<Product> variationSizeSpec;

    public UniqueProductVariantCompositeSpec(ProductVariant productVariant) {
        this.variationSizeSpec = new VariationSizeSpec(productVariant);
        this.combinationSpec = new CombinationSpec(productVariant);
    }


    @Override
    public boolean isSatisfiedBy(Product product) {
        return combinationSpec
                .and(variationSizeSpec)
                .isSatisfiedBy(product);
    }
}
