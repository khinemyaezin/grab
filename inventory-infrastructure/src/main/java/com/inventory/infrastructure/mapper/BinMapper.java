package com.inventory.infrastructure.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.entity.Bin;
import com.inventory.infrastructure.entity.BinEntity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BinMapper {
    private final IdGenerator idGenerator;

    public Bin toDomain(BinEntity entity) {
        if (entity == null) {
            return null;
        }

        Id id = idGenerator.generateId(entity.getUuid());

        Bin bin = new Bin(
                id,
                entity.getCode(),
                entity.getName(),
                entity.getMaxCapacity()
        );
        bin.setActive(entity.isActive());

        return bin;
    }
}
