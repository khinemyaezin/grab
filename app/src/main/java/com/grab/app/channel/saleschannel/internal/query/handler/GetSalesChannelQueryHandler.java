package com.grab.app.channel.saleschannel.internal.query.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import com.saleschannel.application.port.inbound.GetSalesChannelUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetSalesChannelQueryHandler implements QueryHandler<GetSalesChannelQuery, SalesChannelResult> {

    private final GetSalesChannelUseCase getSalesChannelUseCase;

    public GetSalesChannelQueryHandler(GetSalesChannelUseCase getSalesChannelUseCase) {
        this.getSalesChannelUseCase = getSalesChannelUseCase;
    }

    @Override
    @SalesChannelReadTransactional
    public SalesChannelResult handle(GetSalesChannelQuery query) {
        return getSalesChannelUseCase.execute(query);
    }

    @Override
    public Class<GetSalesChannelQuery> getQueryType() {
        return GetSalesChannelQuery.class;
    }
}

