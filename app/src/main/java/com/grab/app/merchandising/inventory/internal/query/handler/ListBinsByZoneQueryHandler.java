package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.ListBinsByZoneQuery;
import com.inventory.application.model.read.ListBinsResult;
import com.inventory.application.port.inbound.ListBinsByZoneUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ListBinsByZoneQueryHandler implements QueryHandler<ListBinsByZoneQuery, Page<ListBinsResult>> {

    private final ListBinsByZoneUseCase listBinsByZoneUseCase;

    public ListBinsByZoneQueryHandler(ListBinsByZoneUseCase listBinsByZoneUseCase) {
        this.listBinsByZoneUseCase = listBinsByZoneUseCase;
    }

    @Override
    @InventoryReadTransactional
    public Page<ListBinsResult> handle(ListBinsByZoneQuery query) {
        return listBinsByZoneUseCase.execute(query);
    }

    @Override
    public Class<ListBinsByZoneQuery> getQueryType() {
        return ListBinsByZoneQuery.class;
    }
}
