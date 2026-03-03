package com.inventory.domain.repository;

import com.grab.framework.id.Id;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.StockMovementType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockMovementRepository {

    void save(StockMovement movement);
    Optional<StockMovement> findById(Id id);
    List<StockMovement> findByInventoryItemId(Id inventoryItemId);
    List<StockMovement> findByInventoryItemIdAndDateRange(Id inventoryItemId, LocalDateTime from,LocalDateTime to);
    List<StockMovement> findByReferenceId(String referenceId);
    List<StockMovement> findByType(StockMovementType type);
    List<StockMovement> findRecentMovements(int days);
    int countByInventoryItemIdAndType(Id inventoryItemId, StockMovementType type);
}
