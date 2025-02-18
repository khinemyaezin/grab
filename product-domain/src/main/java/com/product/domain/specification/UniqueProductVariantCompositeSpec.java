package com.product.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;

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
