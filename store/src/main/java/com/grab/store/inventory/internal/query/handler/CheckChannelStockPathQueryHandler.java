package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.CheckChannelStockPathUseCase;
import com.inventory.application.model.read.CheckChannelStockPathQuery;
import com.inventory.application.model.read.CheckChannelStockPathResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckChannelStockPathQueryHandler implements QueryHandler<CheckChannelStockPathQuery, CheckChannelStockPathResult> {

    private final CheckChannelStockPathUseCase checkChannelStockPathUseCase;

    @Override
    @InventoryReadTransactional
    public CheckChannelStockPathResult handle(CheckChannelStockPathQuery query) {
        return checkChannelStockPathUseCase.execute(query);
    }

    @Override
    public Class<CheckChannelStockPathQuery> getQueryType() {
        return CheckChannelStockPathQuery.class;
    }
}
