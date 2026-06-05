package com.inventory.infrastructure.repository.jpa;

import com.grab.framework.id.Id;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.infrastructure.view.StockMovementView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface StockMovementQueryRepository {
    Page<StockMovementView> queryByInventoryItemId(String inventoryItemId, Pageable pageable);
    Page<StockMovementView> queryByInventoryItemIdAndDateRange(String inventoryItemId, LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<StockMovementView> queryByReferenceId(String referenceId, Pageable pageable);
    Page<StockMovementView> queryByType(StockMovementType type, Pageable pageable);
    Page<StockMovementView> queryRecentMovements(int days, Pageable pageable);
    int countByInventoryItemIdAndType(String inventoryItemId, StockMovementType type);
}
