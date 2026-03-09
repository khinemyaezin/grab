package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.mapper.jpa.LocationJpaAssembler;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultLocationRepository implements LocationRepository {

    private final LocationJpaRepository jpaRepository;
    private final LocationJpaAssembler mapper;

    @Override
    public Optional<Location> findById(Id id) {
        return jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph);
    }

    @Override
    public Optional<Location> findByCode(String code) {
        return jpaRepository.findByCode(code)
                .map(mapper::toFullDomainGraph);
    }

    @Override
    public List<Location> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toFullDomainGraph)
                .toList();
    }

    @Override
    public List<Location> findAllActive() {
        return jpaRepository.findAllByActiveTrue().stream()
                .map(mapper::toFullDomainGraph)
                .toList();
    }

    @Override
    public List<Location> findByType(LocationType type) {
        return jpaRepository.findAllByType(type).stream()
                .map(mapper::toFullDomainGraph)
                .toList();
    }

    @Override
    public Location save(Location location) {
        Optional<LocationEntity> existingEntity = jpaRepository.findByUuid(location.getId().getValue());
        LocationEntity entity;

        if (existingEntity.isPresent()) {
            entity = mapper.buildFullEntityGraph(location, existingEntity.get());
        } else {
            entity = mapper.buildFullEntityGraph(location, null);
        }
        LocationEntity saved = jpaRepository.save(entity);
        return mapper.toFullDomainGraph(saved);
    }

    @Override
    public void delete(Id id) {
        jpaRepository.findByUuid(id.getValue())
                .ifPresent(jpaRepository::delete);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }
}
