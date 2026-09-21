package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.GetBinLocationIdUseCase;
import com.inventory.application.model.read.GetBinLocationIdQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetBinLocationIdQueryHandler implements QueryHandler<GetBinLocationIdQuery, String> {

    private final GetBinLocationIdUseCase getBinLocationIdUseCase;

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
