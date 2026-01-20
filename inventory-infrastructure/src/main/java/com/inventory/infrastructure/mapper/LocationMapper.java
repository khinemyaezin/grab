package com.inventory.infrastructure.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.valueobject.Address;
import com.inventory.infrastructure.entity.LocationEntity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LocationMapper {
    private final IdGenerator idGenerator;

    public Location toDomain(LocationEntity entity) {
        if (entity == null) {
            return null;
        }

        Id id = idGenerator.generateId(entity.getUuid());

        Address address = new Address(
                entity.getStreet(),
                entity.getStreet2(),
                entity.getCity(),
                entity.getState(),
                entity.getPostalCode(),
                entity.getCountry()
        );

        Location location = new Location(
                id,
                entity.getCode(),
                entity.getName(),
                entity.getType(),
                address
        );
        location.setActive(entity.isActive());

        return location;
    }
}
