package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetInventoryReservationsQuery;
import com.inventory.application.model.read.GetInventoryReservationsResult;
import com.inventory.application.port.inbound.GetInventoryReservationUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class GetInventoryReservationQueryHandler implements QueryHandler<GetInventoryReservationsQuery, Page<GetInventoryReservationsResult>> {

    private final GetInventoryReservationUseCase getInventoryReservationUseCase;

    public GetInventoryReservationQueryHandler(GetInventoryReservationUseCase getInventoryReservationUseCase) {
        this.getInventoryReservationUseCase = getInventoryReservationUseCase;
    }

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
