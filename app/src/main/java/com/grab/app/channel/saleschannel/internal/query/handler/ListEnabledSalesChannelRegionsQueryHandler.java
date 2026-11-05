package com.grab.app.channel.saleschannel.internal.query.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.saleschannel.application.model.read.ListEnabledSalesChannelRegionsQuery;
import com.saleschannel.application.port.inbound.ListEnabledSalesChannelRegionsUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListEnabledSalesChannelRegionsQueryHandler
        implements QueryHandler<ListEnabledSalesChannelRegionsQuery, List<Id>> {

    private final ListEnabledSalesChannelRegionsUseCase useCase;

    public ListEnabledSalesChannelRegionsQueryHandler(ListEnabledSalesChannelRegionsUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SalesChannelReadTransactional
    public List<Id> handle(ListEnabledSalesChannelRegionsQuery query) {
        return useCase.execute(query.salesChannelId());
    }

    @Override
    public Class<ListEnabledSalesChannelRegionsQuery> getQueryType() {
        return ListEnabledSalesChannelRegionsQuery.class;
    }
}

