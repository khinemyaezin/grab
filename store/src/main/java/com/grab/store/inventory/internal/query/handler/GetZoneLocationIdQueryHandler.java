package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetZoneLocationIdUseCase;
import com.inventory.application.model.read.GetZoneLocationIdQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetZoneLocationIdQueryHandler implements QueryHandler<GetZoneLocationIdQuery, String> {

    private final GetZoneLocationIdUseCase getZoneLocationIdUseCase;

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
