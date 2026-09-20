package com.saleschannel.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.saleschannel.adapter.persistence.specification.SalesChannelQuerySpecification;
import com.saleschannel.application.model.read.SalesChannelQueryCriteria;
import com.saleschannel.application.model.read.SalesChannelView;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@RequiredArgsConstructor
public class SalesChannelQueryAdapter implements SalesChannelQueryPort {
    private final SalesChannelQuerySpecification specification;
    private final PersistenceExecutor executor;

    @Override
    public Page<SalesChannelView> list(SalesChannelQueryCriteria criteria, Pageable pageable) {
        return executor.query("SalesChannel", () -> specification.list(criteria, pageable));
    }

    @Override
    public Optional<SalesChannelView> findById(String id) {
        return executor.query("SalesChannel", () -> specification.findById(id));
    }
}
