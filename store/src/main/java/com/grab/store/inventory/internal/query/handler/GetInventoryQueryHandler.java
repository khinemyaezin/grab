package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.grab.framework.id.Id;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import com.inventory.infrastructure.view.ProductView;
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
    private final ProductVariantViewJpaRepository productVariantViewJpaRepository;
    private final LocationRepository locationRepository;

    @Override
    @InventoryReadTransactional
    public GetInventoryResult handle(GetInventoryQuery query) {
        InventoryItem item = inventoryRepository.findById(query.inventoryItemId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.InventoryNotFound(query.inventoryItemId().getValue())));
        String productName = resolveProductName(item.getSku());
        Location location = resolveLocation(item.getLocationId());
        return mapToResult(item, productName, location);
    }

    @Override
    public Class<GetInventoryQuery> getQueryType() {
        return GetInventoryQuery.class;
    }

    private String resolveProductName(String sku) {
        if (sku == null) {
            return null;
        }
        return productVariantViewJpaRepository
                .findBySkuContainingIgnoreCaseAndStatus(sku, ProductVariantViewEntity.STATUS_ACTIVE)
                .stream()
                .map(ProductView::getProductName)
                .findFirst()
                .orElse(null);
    }

    private Location resolveLocation(Id locationId) {
        if (locationId == null) {
            return null;
        }
        return locationRepository.findById(locationId)
                .orElse(null);
    }

    private GetInventoryResult mapToResult(InventoryItem item, String productName, Location location) {
        return new GetInventoryResult(
                item.getId(),
                item.getSku(),
                productName,
                item.getLocationId(),
                location.getCode(),
                location.getName(),
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
