package com.grab.store.saleschannel.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.saleschannel.application.model.read.ListSalesChannelsQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import com.saleschannel.application.port.inbound.ListSalesChannelsUseCase;
import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListSalesChannelsQueryHandler
        implements QueryHandler<ListSalesChannelsQuery, Page<SalesChannelResult>> {

    private final ListSalesChannelsUseCase listSalesChannelsUseCase;

    @Override
    @SalesChannelReadTransactional
    public Page<SalesChannelResult> handle(ListSalesChannelsQuery query) {
        return listSalesChannelsUseCase.execute(query);
    }

    @Override
    public Class<ListSalesChannelsQuery> getQueryType() {
        return ListSalesChannelsQuery.class;
    }
}
