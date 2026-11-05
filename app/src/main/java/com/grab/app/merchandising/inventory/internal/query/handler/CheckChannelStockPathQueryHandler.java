package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.CheckChannelStockPathQuery;
import com.inventory.application.model.read.CheckChannelStockPathResult;
import com.inventory.application.port.inbound.CheckChannelStockPathUseCase;
import org.springframework.stereotype.Component;

@Component
public class CheckChannelStockPathQueryHandler implements QueryHandler<CheckChannelStockPathQuery, CheckChannelStockPathResult> {

    private final CheckChannelStockPathUseCase checkChannelStockPathUseCase;

    public CheckChannelStockPathQueryHandler(CheckChannelStockPathUseCase checkChannelStockPathUseCase) {
        this.checkChannelStockPathUseCase = checkChannelStockPathUseCase;
    }

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
