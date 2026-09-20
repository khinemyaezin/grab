package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetInventorySummaryUseCase;
import com.inventory.application.model.read.GetInventorySummaryQuery;
import com.inventory.application.model.read.GetInventorySummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetInventorySummaryQueryHandler implements QueryHandler<GetInventorySummaryQuery, GetInventorySummaryResult> {

    private final GetInventorySummaryUseCase getInventorySummaryUseCase;

    @Override
    @InventoryReadTransactional
    public GetInventorySummaryResult handle(GetInventorySummaryQuery query) {
        return getInventorySummaryUseCase.execute(query);
    }

    @Override
    public Class<GetInventorySummaryQuery> getQueryType() {
        return GetInventorySummaryQuery.class;
    }
}
