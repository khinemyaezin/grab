package com.grab.store.product.internal.query.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.grab.store.product.internal.query.BuildProductQuery;

public class BuildProductSpec extends CompositeSpecification<BuildProductQuery> {
    @Override
    public boolean isSatisfiedBy(BuildProductQuery buildProductRequest) {
        return true;
    }
}
