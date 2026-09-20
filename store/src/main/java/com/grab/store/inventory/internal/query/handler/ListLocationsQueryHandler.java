package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.ListLocationsUseCase;
import com.inventory.application.model.read.ListLocationsQuery;
import com.inventory.application.model.read.ListLocationsResult;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListLocationsQueryHandler implements QueryHandler<ListLocationsQuery, Page<ListLocationsResult>> {

    private final ListLocationsUseCase listLocationsUseCase;

    @Override
    @InventoryReadTransactional
    public Page<ListLocationsResult> handle(ListLocationsQuery query) {
        return listLocationsUseCase.execute(query);
    }

    @Override
    public Class<ListLocationsQuery> getQueryType() {
        return ListLocationsQuery.class;
    }
}
