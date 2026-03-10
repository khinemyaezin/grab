package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
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
    private final PersistenceExecutor executor;

    @Override
    public Optional<Location> findById(Id id) {
        return executor.query("Location", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public Optional<Location> findByCode(String code) {
        return executor.query("Location", () -> jpaRepository.findByCode(code)
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public List<Location> findAll() {
        return executor.query("Location", () -> jpaRepository.findAll().stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public List<Location> findAllActive() {
        return executor.query("Location", () -> jpaRepository.findAllByActiveTrue().stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public List<Location> findByType(LocationType type) {
        return executor.query("Location", () -> jpaRepository.findAllByType(type).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public Location save(Location location) {
        return executor.command("Location", () -> {
            Optional<LocationEntity> existingEntity = jpaRepository.findByUuid(location.getId().getValue());
            LocationEntity entity;

            if (existingEntity.isPresent()) {
                entity = mapper.buildFullEntityGraph(location, existingEntity.get());
            } else {
                entity = mapper.buildFullEntityGraph(location, null);
            }
            LocationEntity saved = jpaRepository.save(entity);
            return mapper.toFullDomainGraph(saved);
        });
    }

    @Override
    public void delete(Id id) {
        executor.command("Location", () -> {
            jpaRepository.findByUuid(id.getValue())
                    .ifPresent(jpaRepository::delete);
            return null;
        });
    }

    @Override
    public boolean existsByCode(String code) {
        return executor.query("Location", () -> jpaRepository.existsByCode(code));
    }
}
