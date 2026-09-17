package com.merchant.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.merchant.infrastructure.repository.jpa.StorefrontQueryRepository;
import com.merchant.infrastructure.specification.jpa.StorefrontQueryCriteria;
import com.merchant.infrastructure.specification.jpa.StorefrontQuerySpecification;
import com.merchant.infrastructure.view.StorefrontView;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultStorefrontQueryRepository implements StorefrontQueryRepository {
    private final StorefrontQuerySpecification specification;
    private final PersistenceExecutor executor;

    @Override
    public List<StorefrontView> list(StorefrontQueryCriteria criteria) {
        return executor.query("Storefront", () -> specification.list(criteria));
    }
}
