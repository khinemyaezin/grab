package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetInventoryQuery;
import com.inventory.application.model.read.GetInventoryResult;
import com.inventory.application.port.inbound.GetInventoryUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetInventoryQueryHandler implements QueryHandler<GetInventoryQuery, GetInventoryResult> {

    private final GetInventoryUseCase getInventoryUseCase;

    public GetInventoryQueryHandler(GetInventoryUseCase getInventoryUseCase) {
        this.getInventoryUseCase = getInventoryUseCase;
    }

    @Override
    @InventoryReadTransactional
    public GetInventoryResult handle(GetInventoryQuery query) {
        return getInventoryUseCase.execute(query);
    }

    @Override
    public Class<GetInventoryQuery> getQueryType() {
        return GetInventoryQuery.class;
    }
}
