package com.grab.app.channel.saleschannel.internal.query.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.saleschannel.application.model.read.CheckSalesChannelUsableQuery;
import com.saleschannel.application.model.read.CheckSalesChannelUsableResult;
import com.saleschannel.application.port.inbound.CheckSalesChannelUsableUseCase;
import org.springframework.stereotype.Component;

@Component
public class CheckSalesChannelUsableQueryHandler
        implements QueryHandler<CheckSalesChannelUsableQuery, CheckSalesChannelUsableResult> {

    private final CheckSalesChannelUsableUseCase checkSalesChannelUsableUseCase;

    public CheckSalesChannelUsableQueryHandler(CheckSalesChannelUsableUseCase checkSalesChannelUsableUseCase) {
        this.checkSalesChannelUsableUseCase = checkSalesChannelUsableUseCase;
    }

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

