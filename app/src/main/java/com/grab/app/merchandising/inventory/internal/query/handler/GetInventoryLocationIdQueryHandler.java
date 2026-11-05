package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetInventoryLocationIdQuery;
import com.inventory.application.port.inbound.GetInventoryLocationIdUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetInventoryLocationIdQueryHandler implements QueryHandler<GetInventoryLocationIdQuery, String> {

    private final GetInventoryLocationIdUseCase getInventoryLocationIdUseCase;

    public GetInventoryLocationIdQueryHandler(GetInventoryLocationIdUseCase getInventoryLocationIdUseCase) {
        this.getInventoryLocationIdUseCase = getInventoryLocationIdUseCase;
    }

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
