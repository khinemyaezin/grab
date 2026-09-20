package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetInventoryLocationIdUseCase;
import com.inventory.application.model.read.GetInventoryLocationIdQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetInventoryLocationIdQueryHandler implements QueryHandler<GetInventoryLocationIdQuery, String> {

    private final GetInventoryLocationIdUseCase getInventoryLocationIdUseCase;

    @Override
    @InventoryReadTransactional
    public String handle(GetInventoryLocationIdQuery query) {
        return getInventoryLocationIdUseCase.execute(query);
    }

    @Override
    public Class<GetInventoryLocationIdQuery> getQueryType() {
        return GetInventoryLocationIdQuery.class;
    }
}
