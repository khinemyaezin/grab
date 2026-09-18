package com.saleschannel.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.saleschannel.infrastructure.repository.jpa.SalesChannelQueryRepository;
import com.saleschannel.infrastructure.specification.jpa.SalesChannelQueryCriteria;
import com.saleschannel.infrastructure.specification.jpa.SalesChannelQuerySpecification;
import com.saleschannel.infrastructure.view.SalesChannelView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DefaultSalesChannelQueryRepository implements SalesChannelQueryRepository {
    private final SalesChannelQuerySpecification specification;
    private final PersistenceExecutor executor;

    @Override
    public Page<SalesChannelView> list(SalesChannelQueryCriteria criteria, Pageable pageable) {
        return executor.query("SalesChannel", () -> specification.list(criteria, pageable));
    }
}
