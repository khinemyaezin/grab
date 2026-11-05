package com.grab.app.channel.saleschannel.internal.query.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.saleschannel.application.model.read.ListSalesChannelsQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import com.saleschannel.application.port.inbound.ListSalesChannelsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ListSalesChannelsQueryHandler
        implements QueryHandler<ListSalesChannelsQuery, Page<SalesChannelResult>> {

    private final ListSalesChannelsUseCase listSalesChannelsUseCase;

    public ListSalesChannelsQueryHandler(ListSalesChannelsUseCase listSalesChannelsUseCase) {
        this.listSalesChannelsUseCase = listSalesChannelsUseCase;
    }

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

