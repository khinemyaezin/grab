package com.grab.app.channel.saleschannel.internal.query.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.saleschannel.application.model.read.FindDefaultSalesChannelRegionQuery;
import com.saleschannel.application.port.inbound.FindDefaultSalesChannelRegionUseCase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FindDefaultSalesChannelRegionQueryHandler
        implements QueryHandler<FindDefaultSalesChannelRegionQuery, Optional<Id>> {

    private final FindDefaultSalesChannelRegionUseCase useCase;

    public FindDefaultSalesChannelRegionQueryHandler(FindDefaultSalesChannelRegionUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SalesChannelReadTransactional
    public Optional<Id> handle(FindDefaultSalesChannelRegionQuery query) {
        return useCase.execute(query.salesChannelId());
    }

    @Override
    public Class<FindDefaultSalesChannelRegionQuery> getQueryType() {
        return FindDefaultSalesChannelRegionQuery.class;
    }
}

