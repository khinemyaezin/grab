package com.grab.app.channeldiscovery.saleschannel.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.grab.app.channeldiscovery.saleschannel.internal.config.SalesChannelReadTransactional;
import com.saleschannel.application.model.read.ListEnabledSalesChannelRegionsQuery;
import com.saleschannel.application.port.inbound.ListEnabledSalesChannelRegionsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListEnabledSalesChannelRegionsQueryHandler
        implements QueryHandler<ListEnabledSalesChannelRegionsQuery, List<Id>> {
    private final ListEnabledSalesChannelRegionsUseCase useCase;

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
