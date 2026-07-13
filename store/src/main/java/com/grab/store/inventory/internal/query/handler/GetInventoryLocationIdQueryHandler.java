package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.repository.InventoryRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.query.GetInventoryLocationIdQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetInventoryLocationIdQueryHandler implements QueryHandler<GetInventoryLocationIdQuery, String> {

    private final InventoryRepository inventoryRepository;

    @Override
    @InventoryReadTransactional
    public String handle(GetInventoryLocationIdQuery query) {
        return inventoryRepository.findById(query.inventoryItemId())
                .map(item -> item.getLocationId().getValue())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.InventoryNotFound(query.inventoryItemId().getValue())));
    }

    @Override
    public Class<GetInventoryLocationIdQuery> getQueryType() {
        return GetInventoryLocationIdQuery.class;
    }
}
