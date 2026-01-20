package com.inventory.infrastructure.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.entity.Zone;
import com.inventory.infrastructure.entity.ZoneEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ZoneMapper {
    private final IdGenerator idGenerator;

    public Zone toDomain(ZoneEntity entity) {
        if (entity == null) {
            return null;
        }

        Id id = idGenerator.generateId(entity.getUuid());

        Zone zone = new Zone(
                id,
                entity.getCode(),
                entity.getName(),
                entity.getType()
        );
        zone.setActive(entity.isActive());

        return zone;
    }
}
