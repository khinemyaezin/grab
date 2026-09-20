package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.ListBinsByZoneUseCase;
import com.inventory.application.model.read.ListBinsByZoneQuery;
import com.inventory.application.model.read.ListBinsResult;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListBinsByZoneQueryHandler implements QueryHandler<ListBinsByZoneQuery, Page<ListBinsResult>> {

    private final ListBinsByZoneUseCase listBinsByZoneUseCase;

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
