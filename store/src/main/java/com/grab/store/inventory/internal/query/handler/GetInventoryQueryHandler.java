package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.repository.InventoryRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.query.GetInventoryQuery;
import com.grab.store.inventory.internal.query.GetInventoryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetInventoryQueryHandler implements QueryHandler<GetInventoryQuery, GetInventoryResult> {

    private final InventoryRepository inventoryRepository;

    @Override
    @InventoryReadTransactional
    public GetInventoryResult handle(GetInventoryQuery query) {
        InventoryItem item = inventoryRepository.findById(query.inventoryItemId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.InventoryNotFound(query.inventoryItemId().getValue())));
        return mapToResult(item);
    }

    @Override
    public Class<GetInventoryQuery> getQueryType() {
        return GetInventoryQuery.class;
    }

    private GetInventoryResult mapToResult(InventoryItem item) {
        return new GetInventoryResult(
                item.getId(),
                item.getSku(),
                item.getProductVariantId() == null ? null : item.getProductVariantId().getValue(),
                item.getLocationId(),
                item.getQuantity().onHand(),
                item.getQuantity().reserved(),
                item.getQuantity().damaged(),
                item.getAvailableQuantity(),
                item.getStatus().name(),
                item.getReorderConfig().safetyStock(),
                item.getReorderConfig().reorderPoint(),
                item.getReorderConfig().reorderQuantity(),
                item.getReorderConfig().maxStock()
        );
    }
}
