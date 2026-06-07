package com.inventory.infrastructure.mapper.jpa.impl;

import com.inventory.domain.aggregate.Location;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.mapper.jpa.LocationEntityMapper;
import com.inventory.infrastructure.mapper.jpa.LocationJpaAssembler;
import com.inventory.infrastructure.mapper.jpa.LocationMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LocationJpaAssemblerImpl implements LocationJpaAssembler {
    private final LocationEntityMapper locationEntityMapper;
    private final LocationMapper locationMapper;

    @Override
    public LocationEntity buildFullEntityGraph(Location location, LocationEntity entity) {
        if (entity == null) {
            entity = new LocationEntity();
        }
        locationEntityMapper.toEntity(location, entity);
        return entity;
    }

    @Override
    public Location toFullDomainGraph(LocationEntity entity) {
        return locationMapper.toDomain(entity);
    }
}
