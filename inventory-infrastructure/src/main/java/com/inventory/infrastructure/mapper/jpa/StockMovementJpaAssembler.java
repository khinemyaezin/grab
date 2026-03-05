package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.entity.StockMovement;
import com.inventory.infrastructure.entity.StockMovementEntity;

public interface StockMovementJpaAssembler {
    /**
     * If `entity` is null a new StockMovementEntity instance will be created; otherwise the provided `entity` is updated.
     *
     * @param movement the StockMovement entity to persist
     * @param entity the existing StockMovementEntity to update, or null to create a new instance
     * @return the resulting StockMovementEntity populated from the domain entity
     */
    StockMovementEntity buildFullEntityGraph(StockMovement movement, StockMovementEntity entity);

    /**
     * Reconstruct the StockMovement domain entity from the provided JPA entity.
     *
     * @param stockMovementEntity the persisted StockMovementEntity to convert (may not be null)
     * @return a fully populated StockMovement domain entity
     */
    StockMovement toFullDomainGraph(StockMovementEntity stockMovementEntity);
}
