package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetInventoryReservationUseCase;
import com.inventory.application.model.read.GetInventoryReservationsQuery;
import com.inventory.application.model.read.GetInventoryReservationsResult;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetInventoryReservationQueryHandler implements QueryHandler<GetInventoryReservationsQuery, Page<GetInventoryReservationsResult>> {

    private final GetInventoryReservationUseCase getInventoryReservationUseCase;

    @Override
    @InventoryReadTransactional
    public Page<GetInventoryReservationsResult> handle(GetInventoryReservationsQuery query) {
        return getInventoryReservationUseCase.execute(query);
    }

    @Override
    public Class<GetInventoryReservationsQuery> getQueryType() {
        return GetInventoryReservationsQuery.class;
    }
}
