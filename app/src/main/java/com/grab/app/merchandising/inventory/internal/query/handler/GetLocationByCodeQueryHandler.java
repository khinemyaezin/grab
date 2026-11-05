package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetLocationByCodeQuery;
import com.inventory.application.model.read.GetLocationResult;
import com.inventory.application.port.inbound.GetLocationByCodeUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetLocationByCodeQueryHandler implements QueryHandler<GetLocationByCodeQuery, GetLocationResult> {

    private final GetLocationByCodeUseCase getLocationByCodeUseCase;

    public GetLocationByCodeQueryHandler(GetLocationByCodeUseCase getLocationByCodeUseCase) {
        this.getLocationByCodeUseCase = getLocationByCodeUseCase;
    }

    @Override
    @InventoryReadTransactional
    public GetLocationResult handle(GetLocationByCodeQuery query) {
        return getLocationByCodeUseCase.execute(query);
    }

    @Override
    public Class<GetLocationByCodeQuery> getQueryType() {
        return GetLocationByCodeQuery.class;
    }
}
