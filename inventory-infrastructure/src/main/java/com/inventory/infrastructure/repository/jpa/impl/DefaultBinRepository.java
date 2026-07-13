package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.repository.BinRepository;
import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.mapper.jpa.BinJpaAssembler;
import com.inventory.infrastructure.repository.jpa.BinJpaRepository;
import com.inventory.infrastructure.view.BinView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultBinRepository implements BinRepository {

    private static final Logger log = Loggers.getLogger(DefaultBinRepository.class);

    private final BinJpaRepository jpaRepository;
    private final BinJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<Bin> findById(Id id) {
        log.debug("Loading bin by id={}", id.getValue());
        return executor.query("Bin", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toDomain));
    }

    @Override
    public Optional<Bin> findByCodeAndZoneId(String code, Id zoneId) {
        log.debug("Loading bin by code={} and zoneId={}", code, zoneId.getValue());
        return executor.query("Bin", () -> jpaRepository.findByCodeAndZoneId(code, zoneId.getValue())
                .map(mapper::toDomain));
    }

    @Override
    public List<Bin> findAllActiveByZoneId(Id zoneId) {
        log.debug("Loading active bins by zoneId={}", zoneId.getValue());
        return executor.query("Bin", () -> jpaRepository.findAllByZoneIdAndActive(zoneId.getValue(), true)
                .stream()
                .map(mapper::toDomain)
                .toList());
    }


    @Override
    public Bin save(Bin bin) {
        return executor.command("Bin", () -> {
            log.info("Persisting bin id={}, code={}", bin.getId().getValue(), bin.getCode());
            Optional<BinEntity> existingEntity = jpaRepository.findByUuid(bin.getId().getValue());
            BinEntity entity = mapper.toEntity(bin, existingEntity.orElse(null));
            BinEntity saved = jpaRepository.save(entity);

            List<Event> events = bin.pullEvents();
            domainEventProducer.produce(bin.getClass().getSimpleName(), bin.getId().getValue(), events);
            log.info("Persisted bin id={}, code={}, publishedEvents={}", bin.getId().getValue(), bin.getCode(), events.size());

            return mapper.toDomain(saved);
        });
    }

    @Override
    public void delete(Id id) {
        executor.command("Bin", () -> {
            log.info("Deleting bin id={}", id.getValue());
            jpaRepository.findByUuid(id.getValue())
                    .ifPresent(jpaRepository::delete);
            return null;
        });
    }

    @Override
    public boolean existsByCodeAndZoneId(String code, Id zoneId) {
        log.debug("Checking bin existence by code={} and zoneId={}", code, zoneId.getValue());
        return executor.query("Bin", () -> jpaRepository.existsByCodeAndZoneId(code, zoneId.getValue()));
    }

    @Override
    public boolean existsByZoneId(Id zoneId) {
        log.debug("Checking bin existence by zoneId={}", zoneId.getValue());
        return executor.query("Bin", () -> jpaRepository.existsByZoneId(zoneId.getValue()));
    }
}
