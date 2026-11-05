package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.GetBinLocationIdQuery;
import com.inventory.application.port.inbound.GetBinLocationIdUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetBinLocationIdQueryHandler implements QueryHandler<GetBinLocationIdQuery, String> {

    private final GetBinLocationIdUseCase getBinLocationIdUseCase;

    public GetBinLocationIdQueryHandler(GetBinLocationIdUseCase getBinLocationIdUseCase) {
        this.getBinLocationIdUseCase = getBinLocationIdUseCase;
    }

    @Override
    @InventoryReadTransactional
    public String handle(GetBinLocationIdQuery query) {
        return getBinLocationIdUseCase.execute(query);
    }

    @Override
    public Class<GetBinLocationIdQuery> getQueryType() {
        return GetBinLocationIdQuery.class;
    }
}
