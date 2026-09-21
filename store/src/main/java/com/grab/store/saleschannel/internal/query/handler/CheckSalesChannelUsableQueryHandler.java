package com.grab.store.saleschannel.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.saleschannel.application.model.read.CheckSalesChannelUsableQuery;
import com.saleschannel.application.model.read.CheckSalesChannelUsableResult;
import com.saleschannel.application.port.inbound.CheckSalesChannelUsableUseCase;
import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckSalesChannelUsableQueryHandler
        implements QueryHandler<CheckSalesChannelUsableQuery, CheckSalesChannelUsableResult> {

    private final CheckSalesChannelUsableUseCase checkSalesChannelUsableUseCase;

    @Override
    @SalesChannelReadTransactional
    public CheckSalesChannelUsableResult handle(CheckSalesChannelUsableQuery query) {
        return checkSalesChannelUsableUseCase.execute(query);
    }

    @Override
    public Class<CheckSalesChannelUsableQuery> getQueryType() {
        return CheckSalesChannelUsableQuery.class;
    }
}
