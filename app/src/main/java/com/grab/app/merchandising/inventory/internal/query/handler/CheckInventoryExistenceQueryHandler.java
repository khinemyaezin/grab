package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.CheckInventoryExistenceQuery;
import com.inventory.application.model.read.CheckInventoryExistenceResult;
import com.inventory.application.port.inbound.CheckInventoryExistenceUseCase;
import org.springframework.stereotype.Component;

@Component
public class CheckInventoryExistenceQueryHandler implements QueryHandler<CheckInventoryExistenceQuery, CheckInventoryExistenceResult> {

    private final CheckInventoryExistenceUseCase checkInventoryExistenceUseCase;

    public CheckInventoryExistenceQueryHandler(CheckInventoryExistenceUseCase checkInventoryExistenceUseCase) {
        this.checkInventoryExistenceUseCase = checkInventoryExistenceUseCase;
    }

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
