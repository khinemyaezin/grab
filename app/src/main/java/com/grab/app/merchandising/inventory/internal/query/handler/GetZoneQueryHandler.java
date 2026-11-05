package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetZoneQuery;
import com.inventory.application.model.read.GetZoneResult;
import com.inventory.application.port.inbound.GetZoneUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetZoneQueryHandler implements QueryHandler<GetZoneQuery, GetZoneResult> {

    private final GetZoneUseCase getZoneUseCase;

    public GetZoneQueryHandler(GetZoneUseCase getZoneUseCase) {
        this.getZoneUseCase = getZoneUseCase;
    }

    @Override
    @InventoryReadTransactional
    public GetZoneResult handle(GetZoneQuery query) {
        return getZoneUseCase.execute(query);
    }

    @Override
    public Class<GetZoneQuery> getQueryType() {
        return GetZoneQuery.class;
    }
}
