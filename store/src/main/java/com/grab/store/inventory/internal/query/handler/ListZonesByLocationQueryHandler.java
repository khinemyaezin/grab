package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.ListZonesByLocationUseCase;
import com.inventory.application.model.read.ListZonesByLocationQuery;
import com.inventory.application.model.read.ListZonesResult;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListZonesByLocationQueryHandler implements QueryHandler<ListZonesByLocationQuery, Page<ListZonesResult>> {

    private final ListZonesByLocationUseCase listZonesByLocationUseCase;

    @Override
    @InventoryReadTransactional
    public Page<ListZonesResult> handle(ListZonesByLocationQuery query) {
        return listZonesByLocationUseCase.execute(query);
    }

    @Override
    public Class<ListZonesByLocationQuery> getQueryType() {
        return ListZonesByLocationQuery.class;
    }
}
