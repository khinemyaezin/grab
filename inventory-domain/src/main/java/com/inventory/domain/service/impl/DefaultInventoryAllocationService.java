package com.inventory.domain.service.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.AllocationError;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.service.InventoryAllocationService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DefaultInventoryAllocationService implements InventoryAllocationService {

    private final InventoryRepository inventoryRepository;
    private final IdGenerator idGenerator;

    public DefaultInventoryAllocationService(InventoryRepository inventoryRepository, IdGenerator idGenerator) {
        this.inventoryRepository = inventoryRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public AllocationResult allocateStock(String sku, int quantity, String orderId) {
        if (quantity <= 0) {
            return AllocationResult.failure(sku, quantity, AllocationError.QUANTITY_NOT_POSITIVE);
        }

        List<InventoryItem> availableItems = findAvailableInventory(sku);
        if (availableItems.isEmpty()) {
            return AllocationResult.failure(sku, quantity, AllocationError.NO_AVAILABLE_INVENTORY, sku);
        }

        int totalAvailable = availableItems.stream()
                .mapToInt(InventoryItem::getAvailableQuantity)
                .sum();

        if (totalAvailable < quantity) {
            return AllocationResult.failure(sku, quantity, AllocationError.INSUFFICIENT_STOCK, totalAvailable, quantity);
        }

        List<AllocationDetail> allocations = new ArrayList<>();
        int remaining = quantity;

        for (InventoryItem item : availableItems) {
            if (remaining <= 0) break;

            int toAllocate = Math.min(remaining, item.getAvailableQuantity());
            if (toAllocate > 0) {
                item.reserveStock(toAllocate, orderId, "system", idGenerator.generateId());
                inventoryRepository.save(item);

                allocations.add(new AllocationDetail(item.getId(), item.getLocationId(), toAllocate));
                remaining -= toAllocate;
            }
        }

        if (remaining > 0) {
            return AllocationResult.partialSuccess(sku, quantity, quantity - remaining, allocations);
        }

        return AllocationResult.success(sku, quantity, allocations);
    }

    @Override
    public AllocationResult allocateStockFromLocation(String sku, Id locationId, int quantity, String orderId) {
        if (quantity <= 0) {
            return AllocationResult.failure(sku, quantity, AllocationError.QUANTITY_NOT_POSITIVE);
        }

        Optional<InventoryItem> inventoryItem = inventoryRepository.findBySkuAndLocation(sku, locationId);

        if (inventoryItem.isPresent()) {
            InventoryItem item = inventoryItem.get();

            if (!item.isActive()) {
                return AllocationResult.failure(sku, quantity, AllocationError.INVENTORY_ITEM_NOT_ACTIVE);
            }
            if (item.getAvailableQuantity() < quantity) {
                return AllocationResult.failure(sku, quantity, AllocationError.INSUFFICIENT_STOCK,
                        item.getAvailableQuantity(), quantity);
            }

            item.reserveStock(quantity, orderId, "system", idGenerator.generateId());
            inventoryRepository.save(item);

            List<AllocationDetail> allocations = List.of(
                    new AllocationDetail(item.getId(), locationId, quantity)
            );
            return AllocationResult.success(sku, quantity, allocations);
        } else {
            return AllocationResult.failure(sku, quantity, AllocationError.INVENTORY_NOT_FOUND_AT_LOCATION,
                    sku, locationId.getValue());
        }
    }

    @Override
    public void deallocateStock(String sku, int quantity, String orderId) {
        List<InventoryItem> items = inventoryRepository.findBySku(sku);

        int remaining = quantity;
        for (InventoryItem item : items) {
            if (remaining <= 0) break;

            int reserved = item.getQuantity().reserved();
            if (reserved > 0) {
                int toRelease = Math.min(remaining, reserved);
                item.releaseReservation(toRelease, orderId, "system", idGenerator.generateId());
                inventoryRepository.save(item);
                remaining -= toRelease;
            }
        }
    }

    @Override
    public boolean canAllocate(String sku, int quantity) {
        return getAvailableForAllocation(sku) >= quantity;
    }

    @Override
    public int getAvailableForAllocation(String sku) {
        return inventoryRepository.getTotalAvailableQuantityBySku(sku);
    }

    @Override
    public List<InventoryItem> findAvailableInventory(String sku) {
        return inventoryRepository.findBySku(sku).stream()
                .filter(InventoryItem::isActive)
                .filter(item -> item.getAvailableQuantity() > 0)
                .sorted(Comparator.comparingInt(InventoryItem::getAvailableQuantity).reversed())
                .toList();
    }
}
