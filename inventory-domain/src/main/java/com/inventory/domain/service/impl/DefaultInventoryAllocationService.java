package com.inventory.domain.service.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.exception.AllocationError;
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
    public AllocationResult allocateStock(String sku, int quantity, String orderId, Id createdBy) {
        if (quantity <= 0) {
            return AllocationResult.failure(sku, quantity, new AllocationError.QuantityNotPositive());
        }

        List<InventoryItem> availableItems = findAvailableInventory(sku);
        if (availableItems.isEmpty()) {
            return AllocationResult.failure(sku, quantity, new AllocationError.NoAvailableInventory(sku));
        }

        int totalAvailable = availableItems.stream()
                .mapToInt(InventoryItem::getAvailableQuantity)
                .sum();

        if (totalAvailable < quantity) {
            return AllocationResult.failure(sku, quantity, new AllocationError.InsufficientStock(totalAvailable, quantity));
        }

        List<AllocationDetail> allocations = new ArrayList<>();
        int remaining = quantity;

        for (InventoryItem item : availableItems) {
            if (remaining <= 0) break;

            int toAllocate = Math.min(remaining, item.getAvailableQuantity());
            if (toAllocate > 0) {
                item.reserveStock(toAllocate, orderId, createdBy, idGenerator.generateId());
                inventoryRepository.save(item);

                allocations.add(new AllocationDetail(item.getId(), item.getLocationId(), toAllocate));
                remaining -= toAllocate;
            }
        }

        return AllocationResult.success(sku, quantity, allocations);
    }

    @Override
    public AllocationResult allocateStockFromLocation(String sku, Id locationId, int quantity, String orderId, Id createdBy) {
        if (quantity <= 0) {
            return AllocationResult.failure(sku, quantity, new AllocationError.QuantityNotPositive());
        }

        Optional<InventoryItem> inventoryItem = inventoryRepository.findBySkuAndLocation(sku, locationId);

        if (inventoryItem.isPresent()) {
            InventoryItem item = inventoryItem.get();

            if (!item.isActive()) {
                return AllocationResult.failure(sku, quantity, new AllocationError.InventoryItemNotActive(sku));
            }
            if (item.getAvailableQuantity() < quantity) {
                return AllocationResult.failure(sku, quantity,
                        new AllocationError.InsufficientStock(item.getAvailableQuantity(), quantity));
            }

            item.reserveStock(quantity, orderId, createdBy, idGenerator.generateId());
            inventoryRepository.save(item);

            List<AllocationDetail> allocations = List.of(
                    new AllocationDetail(item.getId(), locationId, quantity)
            );
            return AllocationResult.success(sku, quantity, allocations);
        } else {
            return AllocationResult.failure(sku, quantity,
                    new AllocationError.InventoryNotFoundAtLocation(sku, locationId.getValue()));
        }
    }

    @Override
    public void deallocateStock(String sku, int quantity, String orderId, Id initiatedBy) {
        List<InventoryItem> items = inventoryRepository.findBySku(sku);

        int remaining = quantity;
        for (InventoryItem item : items) {
            if (remaining <= 0) break;

            int reserved = item.getQuantity().reserved();
            if (reserved > 0) {
                int toRelease = Math.min(remaining, reserved);
                item.releaseReservation(toRelease, orderId, initiatedBy, idGenerator.generateId());
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
