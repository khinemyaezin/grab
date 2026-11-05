package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.ListZonesByLocationQuery;
import com.inventory.application.model.read.ListZonesResult;
import com.inventory.application.port.inbound.ListZonesByLocationUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ListZonesByLocationQueryHandler implements QueryHandler<ListZonesByLocationQuery, Page<ListZonesResult>> {

    private final ListZonesByLocationUseCase listZonesByLocationUseCase;

    public ListZonesByLocationQueryHandler(ListZonesByLocationUseCase listZonesByLocationUseCase) {
        this.listZonesByLocationUseCase = listZonesByLocationUseCase;
    }

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
