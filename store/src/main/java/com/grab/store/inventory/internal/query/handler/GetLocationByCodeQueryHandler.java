package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetLocationByCodeUseCase;
import com.inventory.application.model.read.GetLocationByCodeQuery;
import com.inventory.application.model.read.GetLocationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetLocationByCodeQueryHandler implements QueryHandler<GetLocationByCodeQuery, GetLocationResult> {

    private final GetLocationByCodeUseCase getLocationByCodeUseCase;

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
