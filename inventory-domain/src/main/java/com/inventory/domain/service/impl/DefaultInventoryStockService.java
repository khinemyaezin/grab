package com.inventory.domain.service.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.AdjustmentReason;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.service.InventoryStockService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultInventoryStockService implements InventoryStockService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final IdGenerator idGenerator;

    @Override
    public StockMovementResult receiveStock(Id inventoryItemId, int quantity, StockMovementType type,
                                            String referenceId, String notes, Id userId) {
        InventoryItem item = inventoryRepository.findById(inventoryItemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + inventoryItemId.getValue()));

        StockMovement movement = item.receiveStock(quantity, type, referenceId, notes, userId, idGenerator.generateId());

        inventoryRepository.save(item);
        stockMovementRepository.save(movement);

        return new StockMovementResult(item, movement);
    }

    @Override
    public StockMovementResult adjustStock(Id inventoryItemId, int newOnHandQuantity, AdjustmentReason reason,
                                           String notes, Id userId) {
        InventoryItem item = requireItem(inventoryItemId);

        StockMovement movement = item.adjustStock(newOnHandQuantity, reason, notes, userId, idGenerator.generateId());

        inventoryRepository.save(item);
        stockMovementRepository.save(movement);

        return new StockMovementResult(item, movement);
    }

    @Override
    public StockMovementResult markDamaged(Id inventoryItemId, int quantity, String notes, Id userId) {
        InventoryItem item = requireItem(inventoryItemId);
        StockMovement movement = item.markDamaged(quantity, notes, userId, idGenerator.generateId());
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        return new StockMovementResult(item, movement);
    }

    @Override
    public StockMovementResult writeOff(Id inventoryItemId, int quantity, String reason, String notes, Id userId) {
        InventoryItem item = requireItem(inventoryItemId);
        StockMovement movement = item.writeOff(quantity, reason, notes, userId, idGenerator.generateId());
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        return new StockMovementResult(item, movement);
    }

    @Override
    public StockMovementResult returnToVendor(Id inventoryItemId, int quantity, String reason, String notes, Id userId) {
        InventoryItem item = requireItem(inventoryItemId);
        StockMovement movement = item.returnToVendor(quantity, reason, notes, userId, idGenerator.generateId());
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        return new StockMovementResult(item, movement);
    }

    @Override
    public StockMovementResult transferOut(Id inventoryItemId, int quantity, String transferId, Id userId) {
        InventoryItem item = requireItem(inventoryItemId);
        StockMovement movement = item.transferOut(quantity, transferId, userId, idGenerator.generateId());
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        return new StockMovementResult(item, movement);
    }

    @Override
    public StockMovementResult announceInTransit(Id inventoryItemId, int quantity, String referenceId, Id userId) {
        InventoryItem item = requireItem(inventoryItemId);
        StockMovement movement = item.announceInTransit(quantity, referenceId, userId, idGenerator.generateId());
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        return new StockMovementResult(item, movement);
    }

    @Override
    public StockMovementResult receiveInTransit(Id inventoryItemId, int quantity, String referenceId, Id userId) {
        InventoryItem item = requireItem(inventoryItemId);
        StockMovement movement = item.receiveInTransit(quantity, referenceId, userId, idGenerator.generateId());
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        return new StockMovementResult(item, movement);
    }

    private InventoryItem requireItem(Id inventoryItemId) {
        return inventoryRepository.findById(inventoryItemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + inventoryItemId.getValue()));
    }
}
