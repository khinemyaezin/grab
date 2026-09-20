package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetAllocationAvailabilityUseCase;
import com.inventory.application.model.read.GetAllocationAvailabilityQuery;
import com.inventory.application.model.read.GetAllocationAvailabilityResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAllocationAvailabilityQueryHandler implements QueryHandler<GetAllocationAvailabilityQuery, GetAllocationAvailabilityResult> {

    private final GetAllocationAvailabilityUseCase getAllocationAvailabilityUseCase;

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
