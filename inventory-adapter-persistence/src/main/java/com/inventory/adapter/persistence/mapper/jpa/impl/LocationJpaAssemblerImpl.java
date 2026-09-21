package com.inventory.adapter.persistence.mapper.jpa.impl;

import com.inventory.domain.aggregate.Location;
import com.inventory.adapter.persistence.entity.LocationEntity;
import com.inventory.adapter.persistence.mapper.jpa.LocationEntityMapper;
import com.inventory.adapter.persistence.mapper.jpa.LocationJpaAssembler;
import com.inventory.adapter.persistence.mapper.jpa.LocationMapper;
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
