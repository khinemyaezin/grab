package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetInventorySummaryQuery;
import com.inventory.application.model.read.GetInventorySummaryResult;
import com.inventory.application.port.inbound.GetInventorySummaryUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetInventorySummaryQueryHandler implements QueryHandler<GetInventorySummaryQuery, GetInventorySummaryResult> {

    private final GetInventorySummaryUseCase getInventorySummaryUseCase;

    public GetInventorySummaryQueryHandler(GetInventorySummaryUseCase getInventorySummaryUseCase) {
        this.getInventorySummaryUseCase = getInventorySummaryUseCase;
    }

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
