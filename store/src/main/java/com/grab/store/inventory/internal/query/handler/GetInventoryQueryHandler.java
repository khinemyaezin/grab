package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetInventoryUseCase;
import com.inventory.application.model.read.GetInventoryQuery;
import com.inventory.application.model.read.GetInventoryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetInventoryQueryHandler implements QueryHandler<GetInventoryQuery, GetInventoryResult> {

    private final GetInventoryUseCase getInventoryUseCase;

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
