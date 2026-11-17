package com.grab.service.merchandising.inventory.internal.query.handler;

import com.grab.service.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetLocationQuery;
import com.inventory.application.model.read.GetLocationResult;
import com.inventory.application.port.inbound.GetLocationUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetLocationQueryHandler implements QueryHandler<GetLocationQuery, GetLocationResult> {

    private final GetLocationUseCase getLocationUseCase;

    public GetLocationQueryHandler(GetLocationUseCase getLocationUseCase) {
        this.getLocationUseCase = getLocationUseCase;
    }

    @Override
    @InventoryReadTransactional
    public GetLocationResult handle(GetLocationQuery query) {
        return getLocationUseCase.execute(query);
    }

    @Override
    public Class<GetLocationQuery> getQueryType() {
        return GetLocationQuery.class;
    }
}
