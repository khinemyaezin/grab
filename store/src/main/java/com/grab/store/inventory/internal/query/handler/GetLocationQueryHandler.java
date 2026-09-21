package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetLocationUseCase;
import com.inventory.application.model.read.GetLocationQuery;
import com.inventory.application.model.read.GetLocationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetLocationQueryHandler implements QueryHandler<GetLocationQuery, GetLocationResult> {

    private final GetLocationUseCase getLocationUseCase;

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
