package com.inventory.domain.port.outbound;

import com.grab.framework.id.Id;
import com.inventory.domain.entity.StockMovement;

import java.util.Optional;

public interface StockMovementRepository {
    void save(StockMovement movement);
    Optional<StockMovement> findById(Id id);
}
