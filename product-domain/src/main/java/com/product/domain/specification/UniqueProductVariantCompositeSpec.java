package com.product.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;

public class UniqueProductVariantCompositeSpec extends CompositeSpecification<Product> {

    private final CompositeSpecification<Product> combinationSpec;

    public UniqueProductVariantCompositeSpec(ProductVariant productVariant) {
        this.combinationSpec = new UniqueProductVariantSpec(productVariant);
    }


    @Override
    public boolean isSatisfiedBy(Product product) {
        return combinationSpec
                .isSatisfiedBy(product);
    }
}
