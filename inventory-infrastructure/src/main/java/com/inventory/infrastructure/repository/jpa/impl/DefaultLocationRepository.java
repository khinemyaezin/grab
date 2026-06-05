package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.mapper.jpa.LocationJpaAssembler;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationQueryRepository;
import com.inventory.infrastructure.view.LocationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultLocationRepository implements LocationRepository, LocationQueryRepository {

    private static final Logger log = Loggers.getLogger(DefaultLocationRepository.class);

    private final LocationJpaRepository jpaRepository;
    private final LocationJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<Location> findById(Id id) {
        log.debug("Loading location by id={}", id.getValue());
        return executor.query("Location", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toDomain));
    }

    @Override
    public Optional<Location> findByCode(String code) {
        log.debug("Loading location by code={}", code);
        return executor.query("Location", () -> jpaRepository.findByCode(code)
                .map(mapper::toDomain));
    }

    @Override
    public Page<LocationView> queryAll(String sellerId, Pageable pageable) {
        return executor.query("Location", () -> jpaRepository.findAllBySellerId(sellerId, pageable));
    }

    @Override
    public Page<LocationView> queryByActive(String sellerId, Pageable pageable) {
        return executor.query("Location", () ->
                jpaRepository.findAllBySellerIdAndActiveTrue(sellerId, pageable));
    }

    @Override
    public Page<LocationView> queryByType(String sellerId, LocationType type, Pageable pageable) {
        return executor.query("Location", () -> jpaRepository.findAllBySellerIdAndType(sellerId, type, pageable));
    }

    @Override
    public Location save(Location location) {
        return executor.command("Location", () -> {
            log.info("Persisting location id={}, code={}", location.getId().getValue(), location.getCode());
            Optional<LocationEntity> existingEntity = jpaRepository.findByUuid(location.getId().getValue());
            LocationEntity entity = mapper.toEntity(location, existingEntity.orElse(null));
            LocationEntity saved = jpaRepository.save(entity);

            List<Event> events = location.pullEvents();
            domainEventProducer.produce(location.getClass().getSimpleName(), location.getId().getValue(), events);
            log.info("Persisted location id={}, code={}, publishedEvents={}", location.getId().getValue(), location.getCode(), events.size());

            return mapper.toDomain(saved);
        });
    }

    @Override
    public void delete(Id id) {
        executor.command("Location", () -> {
            log.info("Deleting location id={}", id.getValue());
            jpaRepository.findByUuid(id.getValue())
                    .ifPresent(jpaRepository::delete);
            return null;
        });
    }

    @Override
    public boolean existsByCode(String code) {
        log.debug("Checking location existence by code={}", code);
        return executor.query("Location", () -> jpaRepository.existsByCode(code));
    }
}
