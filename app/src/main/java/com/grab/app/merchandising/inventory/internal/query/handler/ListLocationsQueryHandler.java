package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.ListLocationsQuery;
import com.inventory.application.model.read.ListLocationsResult;
import com.inventory.application.port.inbound.ListLocationsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ListLocationsQueryHandler implements QueryHandler<ListLocationsQuery, Page<ListLocationsResult>> {

    private final ListLocationsUseCase listLocationsUseCase;

    public ListLocationsQueryHandler(ListLocationsUseCase listLocationsUseCase) {
        this.listLocationsUseCase = listLocationsUseCase;
    }

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
