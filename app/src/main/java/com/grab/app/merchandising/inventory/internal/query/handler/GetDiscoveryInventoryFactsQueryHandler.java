package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsQuery;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsResult;
import com.inventory.application.port.inbound.GetDiscoveryInventoryFactsUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetDiscoveryInventoryFactsQueryHandler implements QueryHandler<GetDiscoveryInventoryFactsQuery, GetDiscoveryInventoryFactsResult> {

    private final GetDiscoveryInventoryFactsUseCase getDiscoveryInventoryFactsUseCase;

    public GetDiscoveryInventoryFactsQueryHandler(GetDiscoveryInventoryFactsUseCase getDiscoveryInventoryFactsUseCase) {
        this.getDiscoveryInventoryFactsUseCase = getDiscoveryInventoryFactsUseCase;
    }

    @Override
    @InventoryReadTransactional
    public GetDiscoveryInventoryFactsResult handle(GetDiscoveryInventoryFactsQuery query) {
        return getDiscoveryInventoryFactsUseCase.execute(query);
    }

    @Override
    public Class<GetDiscoveryInventoryFactsQuery> getQueryType() {
        return GetDiscoveryInventoryFactsQuery.class;
    }
}
