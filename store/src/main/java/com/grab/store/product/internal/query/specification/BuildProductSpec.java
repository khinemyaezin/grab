package com.grab.store.product.internal.query.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.grab.store.product.internal.query.ProductCombinationQuery;
import org.springframework.stereotype.Component;

@Component
public class BuildProductSpec extends CompositeSpecification<ProductCombinationQuery> {
    @Override
    public boolean isSatisfiedBy(ProductCombinationQuery buildProductRequest) {
        return true;
    }
}
