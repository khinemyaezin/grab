package com.inventory.infrastructure.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.entity.StockMovement;
import com.inventory.infrastructure.entity.StockMovementEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StockMovementMapper {
    private final IdGenerator idGenerator;

    public StockMovement toDomain(StockMovementEntity entity) {
        if (entity == null) {
            return null;
        }

        Id id = idGenerator.generateId(entity.getUuid());
        Id createdBy = entity.getCreatedBy() != null ? idGenerator.generateId(entity.getCreatedBy()) : null;

        return new StockMovement(
                id,
                entity.getType(),
                entity.getQuantity(),
                entity.getQuantityBefore(),
                entity.getQuantityAfter(),
                entity.getReferenceId(),
                entity.getCreatedAt(),
                createdBy
        );
    }
}
