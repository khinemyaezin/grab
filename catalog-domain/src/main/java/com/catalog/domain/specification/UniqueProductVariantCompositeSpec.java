package com.catalog.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;

public class UniqueProductVariantCompositeSpec extends CompositeSpecification<Product> {

    private final CompositeSpecification<Product> combinationSpec;

    public UniqueProductVariantCompositeSpec(ProductVariant productVariant, int ignoredVariantIndex) {
        this.combinationSpec = new UniqueProductVariantSpec(productVariant, ignoredVariantIndex);
    }

    public UniqueProductVariantCompositeSpec(ProductVariant productVariant) {
        this.combinationSpec = new UniqueProductVariantSpec(productVariant);
    }


    @Override
    public boolean isSatisfiedBy(Product product) {
        return combinationSpec
                .isSatisfiedBy(product);
    }
}
