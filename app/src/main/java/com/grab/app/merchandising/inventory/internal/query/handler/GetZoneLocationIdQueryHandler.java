package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetZoneLocationIdQuery;
import com.inventory.application.port.inbound.GetZoneLocationIdUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetZoneLocationIdQueryHandler implements QueryHandler<GetZoneLocationIdQuery, String> {

    private final GetZoneLocationIdUseCase getZoneLocationIdUseCase;

    public GetZoneLocationIdQueryHandler(GetZoneLocationIdUseCase getZoneLocationIdUseCase) {
        this.getZoneLocationIdUseCase = getZoneLocationIdUseCase;
    }

    @Override
    @InventoryReadTransactional
    public String handle(GetZoneLocationIdQuery query) {
        return getZoneLocationIdUseCase.execute(query);
    }

    @Override
    public Class<GetZoneLocationIdQuery> getQueryType() {
        return GetZoneLocationIdQuery.class;
    }
}
