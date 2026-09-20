package com.merchant.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.merchant.adapter.persistence.specification.jpa.StorefrontQuerySpecification;
import com.merchant.application.model.read.StorefrontQueryCriteria;
import com.merchant.application.model.read.StorefrontView;
import com.merchant.application.port.outbound.StorefrontQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class StorefrontQueryAdapter implements StorefrontQueryPort {
    private final StorefrontQuerySpecification specification;
    private final PersistenceExecutor executor;

    @Override
    public List<StorefrontView> list(StorefrontQueryCriteria criteria) {
        return executor.query("Storefront", () -> specification.list(criteria));
    }

    @Override
    public Optional<StorefrontView> findById(String storefrontId) {
        return executor.query("Storefront", () -> specification.findById(storefrontId));
    }
}
