package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetInventoryMovementsQuery;
import com.inventory.application.model.read.GetInventoryMovementsResult;
import com.inventory.application.port.inbound.GetInventoryMovementsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class GetInventoryMovementsQueryHandler implements QueryHandler<GetInventoryMovementsQuery, Page<GetInventoryMovementsResult>> {

    private final GetInventoryMovementsUseCase getInventoryMovementsUseCase;

    public GetInventoryMovementsQueryHandler(GetInventoryMovementsUseCase getInventoryMovementsUseCase) {
        this.getInventoryMovementsUseCase = getInventoryMovementsUseCase;
    }

    @Override
    @InventoryReadTransactional
    public Page<GetInventoryMovementsResult> handle(GetInventoryMovementsQuery query) {
        return getInventoryMovementsUseCase.execute(query);
    }

    @Override
    public Class<GetInventoryMovementsQuery> getQueryType() {
        return GetInventoryMovementsQuery.class;
    }
}
