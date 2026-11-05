package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetBinQuery;
import com.inventory.application.model.read.GetBinResult;
import com.inventory.application.port.inbound.GetBinUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetBinQueryHandler implements QueryHandler<GetBinQuery, GetBinResult> {

    private final GetBinUseCase getBinUseCase;

    public GetBinQueryHandler(GetBinUseCase getBinUseCase) {
        this.getBinUseCase = getBinUseCase;
    }

    @Override
    @InventoryReadTransactional
    public GetBinResult handle(GetBinQuery query) {
        return getBinUseCase.execute(query);
    }

    @Override
    public Class<GetBinQuery> getQueryType() {
        return GetBinQuery.class;
    }
}
