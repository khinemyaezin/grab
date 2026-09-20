package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetInventoryMovementsUseCase;
import com.inventory.application.model.read.GetInventoryMovementsQuery;
import com.inventory.application.model.read.GetInventoryMovementsResult;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetInventoryMovementsQueryHandler implements QueryHandler<GetInventoryMovementsQuery, Page<GetInventoryMovementsResult>> {

    private final GetInventoryMovementsUseCase getInventoryMovementsUseCase;

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
