package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetZoneUseCase;
import com.inventory.application.model.read.GetZoneQuery;
import com.inventory.application.model.read.GetZoneResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetZoneQueryHandler implements QueryHandler<GetZoneQuery, GetZoneResult> {

    private final GetZoneUseCase getZoneUseCase;

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
