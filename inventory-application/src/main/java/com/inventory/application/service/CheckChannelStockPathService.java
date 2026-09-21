package com.inventory.application.service;

import com.inventory.application.port.inbound.CheckChannelStockPathUseCase;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.model.read.CheckChannelStockPathQuery;
import com.inventory.application.model.read.CheckChannelStockPathResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckChannelStockPathService implements CheckChannelStockPathUseCase {

    private final InventoryQueryPort inventoryQueryPort;

    public CheckChannelStockPathResult execute(CheckChannelStockPathQuery query) {
        boolean routeExists = inventoryQueryPort.existsActiveRoute(query.merchantId(), query.salesChannelId());
        return new CheckChannelStockPathResult(routeExists);
    }

    public Class<CheckChannelStockPathQuery> getQueryType() {
        return CheckChannelStockPathQuery.class;
    }
}
