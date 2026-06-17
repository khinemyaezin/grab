package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.repository.ZoneRepository;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.mapper.jpa.ZoneJpaAssembler;
import com.inventory.infrastructure.repository.jpa.ZoneJpaRepository;
import com.inventory.infrastructure.repository.jpa.ZoneQueryRepository;
import com.inventory.infrastructure.view.ZoneView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultZoneRepository implements ZoneRepository, ZoneQueryRepository {

    private static final Logger log = Loggers.getLogger(DefaultZoneRepository.class);

    private final ZoneJpaRepository jpaRepository;
    private final ZoneJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<Zone> findById(Id id) {
        log.debug("Loading zone by id={}", id.getValue());
        return executor.query("Zone", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public List<Zone> findAllActiveByLocationId(Id locationId) {
        log.debug("Loading active zones by locationId={}", locationId.getValue());
        return executor.query("Zone", () -> jpaRepository.findAllByLocationIdAndActive(locationId.getValue(), true)
                .stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public Page<ZoneView> queryByLocationId(String locationId, Pageable pageable) {
        log.debug("Loading zones by locationId={}", locationId);
        return executor.query("Zone", () -> jpaRepository.findAllByLocationId(locationId, pageable));
    }

    @Override
    public Page<ZoneView> queryByLocationIdAndActive(String locationId, boolean active, Pageable pageable) {
        log.debug("Loading zones by locationId={} and active={}", locationId, active);
        return executor.query("Zone", () -> jpaRepository.findAllByLocationIdAndActive(locationId, active, pageable));
    }

    @Override
    public Zone save(Zone zone) {
        return executor.command("Zone", () -> {
            log.info("Persisting zone id={}, code={}", zone.getId().getValue(), zone.getCode());

            Optional<ZoneEntity> existingEntity = jpaRepository.findByUuid(zone.getId().getValue());
            ZoneEntity entity = mapper.buildFullEntityGraph(zone, existingEntity.orElse(null));
            ZoneEntity saved = jpaRepository.save(entity);

            List<Event> events = zone.pullEvents();
            domainEventProducer.produce(zone.getClass().getSimpleName(), zone.getId().getValue(), events);
            log.info("Persisted zone id={}, code={}, publishedEvents={}", zone.getId().getValue(), zone.getCode(), events.size());

            return mapper.toFullDomainGraph(saved);
        });
    }

    @Override
    public void delete(Id id) {
        executor.command("Zone", () -> {
            log.info("Deleting zone id={}", id.getValue());
            jpaRepository.findByUuid(id.getValue())
                    .ifPresent(jpaRepository::delete);
            return null;
        });
    }

    @Override
    public boolean existsByCodeAndLocationId(String code, Id locationId) {
        log.debug("Checking zone existence by code={} and locationId={}", code, locationId.getValue());
        return executor.query("Zone", () -> jpaRepository.existsByCodeAndLocationId(code, locationId.getValue()));
    }

    @Override
    public boolean existsByLocationId(Id locationId) {
        log.debug("Checking zone existence by locationId={}", locationId.getValue());
        return executor.query("Zone", () -> jpaRepository.existsByLocationId(locationId.getValue()));
    }
}
