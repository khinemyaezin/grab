package com.inventory.domain.service.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.exception.InventoryDomainError;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.service.InventoryAllocationService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DefaultInventoryAllocationService implements InventoryAllocationService {

    private static final Logger log = Loggers.getLogger(DefaultInventoryAllocationService.class);

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LocationRepository locationRepository;
    private final IdGenerator idGenerator;

    public DefaultInventoryAllocationService(
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            LocationRepository locationRepository,
            IdGenerator idGenerator
    ) {
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.locationRepository = locationRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public AllocationResult allocateStock(String sku, int quantity, String orderId, Id createdBy) {
        log.info("Allocating stock for sku={}, quantity={}, orderId={}", sku, quantity, orderId);

        if (quantity <= 0) {
            log.warn("Rejected allocation for sku={} because quantity={} is not positive", sku, quantity);
            return AllocationResult.failure(sku, quantity, new InventoryDomainError.QuantityNotPositive());
        }

        List<InventoryItem> availableItems = findAvailableInventory(sku);
        if (availableItems.isEmpty()) {
            log.warn("No allocatable inventory found for sku={}", sku);
            return AllocationResult.failure(sku, quantity, new InventoryDomainError.NoAvailableInventory(sku));
        }

        int totalAvailable = availableItems.stream()
                .mapToInt(InventoryItem::getAvailableQuantity)
                .sum();

        if (totalAvailable < quantity) {
            log.warn(
                    "Insufficient stock for sku={}: requested={}, available={}",
                    sku,
                    quantity,
                    totalAvailable
            );
            return AllocationResult.failure(sku, quantity, new InventoryDomainError.InsufficientStock(totalAvailable, quantity));
        }

        List<AllocationDetail> allocations = new ArrayList<>();
        int remaining = quantity;

        for (InventoryItem item : availableItems) {
            if (remaining <= 0) break;

            int toAllocate = Math.min(remaining, item.getAvailableQuantity());
            if (toAllocate > 0) {
                StockMovement movement = item.reserveStock(toAllocate, orderId, createdBy, idGenerator.generateId());
                inventoryRepository.save(item);
                stockMovementRepository.save(movement);

                log.debug(
                        "Reserved quantity={} for sku={} from inventoryItemId={} at locationId={}",
                        toAllocate,
                        sku,
                        item.getId().getValue(),
                        item.getLocationId().getValue()
                );

                allocations.add(new AllocationDetail(item.getId(), item.getLocationId(), toAllocate));
                remaining -= toAllocate;
            }
        }

        log.info(
                "Allocation completed for sku={}: requested={}, allocations={}, remaining={}",
                sku,
                quantity,
                allocations.size(),
                remaining
        );
        return AllocationResult.success(sku, quantity, allocations);
    }

    @Override
    public AllocationResult allocateStockFromLocation(String sku, Id locationId, int quantity, String orderId, Id createdBy) {
        log.info(
                "Allocating stock from location for sku={}, locationId={}, quantity={}, orderId={}",
                sku,
                locationId.getValue(),
                quantity,
                orderId
        );

        if (quantity <= 0) {
            log.warn("Rejected location allocation for sku={} because quantity={} is not positive", sku, quantity);
            return AllocationResult.failure(sku, quantity, new InventoryDomainError.QuantityNotPositive());
        }

        Optional<InventoryItem> inventoryItem = inventoryRepository.findBySkuAndLocation(sku, locationId);

        Optional<Location> location = locationRepository.findById(locationId);
        if (location.isEmpty() || !location.get().isActive()) {
            log.warn("Location is missing or inactive for sku={} locationId={}", sku, locationId.getValue());
            return AllocationResult.failure(sku, quantity, new InventoryDomainError.InventoryNotFoundAtLocation(sku, locationId.getValue()));
        }

        if (inventoryItem.isPresent()) {
            InventoryItem item = inventoryItem.get();

            if (!item.isActive()) {
                log.warn("Inventory item is not active for sku={} at locationId={}", sku, locationId.getValue());
                return AllocationResult.failure(sku, quantity, new InventoryDomainError.InventoryItemNotActive(sku));
            }
            if (item.getAvailableQuantity() < quantity) {
                log.warn(
                        "Insufficient stock at location for sku={}: requested={}, available={}, locationId={}",
                        sku,
                        quantity,
                        item.getAvailableQuantity(),
                        locationId.getValue()
                );
                return AllocationResult.failure(sku, quantity,
                        new InventoryDomainError.InsufficientStock(item.getAvailableQuantity(), quantity));
            }

            StockMovement movement = item.reserveStock(quantity, orderId, createdBy, idGenerator.generateId());
            inventoryRepository.save(item);
            stockMovementRepository.save(movement);

            log.info(
                    "Allocated quantity={} for sku={} from inventoryItemId={} at locationId={}",
                    quantity,
                    sku,
                    item.getId().getValue(),
                    locationId.getValue()
            );

            List<AllocationDetail> allocations = List.of(
                    new AllocationDetail(item.getId(), locationId, quantity)
            );
            return AllocationResult.success(sku, quantity, allocations);
        } else {
            log.warn("No inventory found for sku={} at locationId={}", sku, locationId.getValue());
            return AllocationResult.failure(sku, quantity,
                    new InventoryDomainError.InventoryNotFoundAtLocation(sku, locationId.getValue()));
        }
    }

    @Override
    public void deallocateStock(String sku, int quantity, String orderId, Id initiatedBy) {
        log.info("Deallocating stock for sku={}, quantity={}, orderId={}", sku, quantity, orderId);

        List<InventoryItem> items = inventoryRepository.findBySku(sku);

        int remaining = quantity;
        for (InventoryItem item : items) {
            if (remaining <= 0) break;

            int reserved = item.getQuantity().reserved();
            if (reserved > 0) {
                int toRelease = Math.min(remaining, reserved);
                StockMovement movement = item.releaseReservation(toRelease, orderId, initiatedBy, idGenerator.generateId());
                inventoryRepository.save(item);
                stockMovementRepository.save(movement);
                log.debug(
                        "Released quantity={} for sku={} from inventoryItemId={} at locationId={}",
                        toRelease,
                        sku,
                        item.getId().getValue(),
                        item.getLocationId().getValue()
                );
                remaining -= toRelease;
            }
        }

        if (remaining > 0) {
            log.warn("Deallocation ended with unreleased quantity={} for sku={}", remaining, sku);
            return;
        }

        log.info("Deallocation completed for sku={}, releasedQuantity={}", sku, quantity);
    }

    @Override
    public boolean canAllocate(String sku, int quantity) {
        return getAvailableForAllocation(sku) >= quantity;
    }

    @Override
    public int getAvailableForAllocation(String sku) {
        return findAvailableInventory(sku).stream()
                .mapToInt(InventoryItem::getAvailableQuantity)
                .sum();
    }

    @Override
    public List<InventoryItem> findAvailableInventory(String sku) {
        List<InventoryItem> availableItems = inventoryRepository.findBySku(sku).stream()
                .filter(InventoryItem::isActive)
                .filter(item -> item.getAvailableQuantity() > 0)
                .filter(item -> locationRepository.findById(item.getLocationId())
                        .map(Location::isActive)
                        .orElse(false))
                .sorted(Comparator.comparingInt(InventoryItem::getAvailableQuantity).reversed())
                .toList();

        log.debug("Found {} allocatable inventory items for sku={}", availableItems.size(), sku);
        return availableItems;
    }
}
