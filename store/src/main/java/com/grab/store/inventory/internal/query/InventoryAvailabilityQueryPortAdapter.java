package com.grab.store.inventory.internal.query;

import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.query.InventoryAvailabilityQueryPort;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryAvailabilityQueryPortAdapter implements InventoryAvailabilityQueryPort {
    private final InventoryAllocationService inventoryAllocationService;
    private final ProductVariantViewJpaRepository productVariantViewJpaRepository;
    private final InventoryRepository inventoryRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Availability available(String sku, String salesChannelId) {
        boolean untracked = productVariantViewJpaRepository
                .findBySkuAndStatus(sku, ProductVariantViewEntity.STATUS_ACTIVE)
                .map(view -> !view.isManageInventory())
                .orElse(false);
        if (untracked) {
            return new Availability(0, true);
        }
        int qty = inventoryAllocationService.getAvailableForAllocation(
                sku,
                salesChannelId == null ? null : idGenerator.convertIdFrom(salesChannelId)
        );
        return new Availability(qty, false);
    }

    @Override
    @InventoryReadTransactional
    public List<String> skusAtLocation(String locationId) {
        return inventoryRepository.findByLocation(idGenerator.convertIdFrom(locationId)).stream()
                .map(InventoryItem::getSku)
                .distinct()
                .toList();
    }
}
