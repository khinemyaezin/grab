package com.grab.store.saleschannel.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.store.saleschannel.internal.query.ListSalesChannelsQuery;
import com.grab.store.saleschannel.internal.query.SalesChannelResult;
import com.saleschannel.infrastructure.repository.jpa.SalesChannelQueryRepository;
import com.saleschannel.infrastructure.specification.jpa.SalesChannelQueryCriteria;
import com.saleschannel.infrastructure.view.SalesChannelView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListSalesChannelsQueryHandler
        implements QueryHandler<ListSalesChannelsQuery, Page<SalesChannelResult>> {

    private final SalesChannelQueryRepository salesChannelQueryRepository;

    @Override
    @SalesChannelReadTransactional
    public Page<SalesChannelResult> handle(ListSalesChannelsQuery query) {
        return salesChannelQueryRepository
                .list(new SalesChannelQueryCriteria(query.merchantId()), query.pageable())
                .map(this::toResult);
    }

    @Override
    public Class<ListSalesChannelsQuery> getQueryType() {
        return ListSalesChannelsQuery.class;
    }

    private SalesChannelResult toResult(SalesChannelView view) {
        return new SalesChannelResult(
                view.id(),
                view.name(),
                view.type() == null ? null : view.type().name(),
                view.owner() == null ? null : view.owner().name(),
                view.merchantId(),
                view.status() == null ? null : view.status().name()
        );
    }
}
