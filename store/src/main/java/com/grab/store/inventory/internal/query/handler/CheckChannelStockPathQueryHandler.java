package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.CheckChannelStockPathQuery;
import com.grab.store.inventory.internal.query.CheckChannelStockPathResult;
import com.inventory.domain.repository.ChannelFulfillmentRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckChannelStockPathQueryHandler
        implements QueryHandler<CheckChannelStockPathQuery, CheckChannelStockPathResult> {

    private final ChannelFulfillmentRouteRepository channelFulfillmentRouteRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public CheckChannelStockPathResult handle(CheckChannelStockPathQuery query) {
        boolean routeExists = channelFulfillmentRouteRepository.existsActiveForMerchantAndChannel(
                idGenerator.convertIdFrom(query.merchantId()),
                idGenerator.convertIdFrom(query.salesChannelId())
        );
        return new CheckChannelStockPathResult(routeExists);
    }

    @Override
    public Class<CheckChannelStockPathQuery> getQueryType() {
        return CheckChannelStockPathQuery.class;
    }
}
