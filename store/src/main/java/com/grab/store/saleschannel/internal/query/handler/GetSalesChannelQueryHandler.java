package com.grab.store.saleschannel.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import com.saleschannel.application.port.inbound.GetSalesChannelUseCase;
import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSalesChannelQueryHandler implements QueryHandler<GetSalesChannelQuery, SalesChannelResult> {

    private final GetSalesChannelUseCase getSalesChannelUseCase;

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
