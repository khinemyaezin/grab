package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.CheckInventoryExistenceUseCase;
import com.inventory.application.model.read.CheckInventoryExistenceQuery;
import com.inventory.application.model.read.CheckInventoryExistenceResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckInventoryExistenceQueryHandler implements QueryHandler<CheckInventoryExistenceQuery, CheckInventoryExistenceResult> {

    private final CheckInventoryExistenceUseCase checkInventoryExistenceUseCase;

    @Override
    @InventoryReadTransactional
    public CheckInventoryExistenceResult handle(CheckInventoryExistenceQuery query) {
        return checkInventoryExistenceUseCase.execute(query);
    }

    @Override
    public Class<CheckInventoryExistenceQuery> getQueryType() {
        return CheckInventoryExistenceQuery.class;
    }
}
