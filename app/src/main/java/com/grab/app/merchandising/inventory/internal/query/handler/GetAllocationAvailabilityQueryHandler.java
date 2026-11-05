package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetAllocationAvailabilityQuery;
import com.inventory.application.model.read.GetAllocationAvailabilityResult;
import com.inventory.application.port.inbound.GetAllocationAvailabilityUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetAllocationAvailabilityQueryHandler implements QueryHandler<GetAllocationAvailabilityQuery, GetAllocationAvailabilityResult> {

    private final GetAllocationAvailabilityUseCase getAllocationAvailabilityUseCase;

    public GetAllocationAvailabilityQueryHandler(GetAllocationAvailabilityUseCase getAllocationAvailabilityUseCase) {
        this.getAllocationAvailabilityUseCase = getAllocationAvailabilityUseCase;
    }

    @Override
    @InventoryReadTransactional
    public GetAllocationAvailabilityResult handle(GetAllocationAvailabilityQuery query) {
        return getAllocationAvailabilityUseCase.execute(query);
    }

    @Override
    public Class<GetAllocationAvailabilityQuery> getQueryType() {
        return GetAllocationAvailabilityQuery.class;
    }
}
