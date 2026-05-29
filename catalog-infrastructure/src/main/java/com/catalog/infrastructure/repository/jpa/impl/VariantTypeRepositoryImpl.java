package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.domain.aggregate.VariantType;
import com.catalog.domain.repository.VariantTypeRepository;
import com.catalog.infrastructure.entity.entity.VariantTypeEntity;
import com.catalog.infrastructure.mapper.jpa.VariantTypeJpaAssembler;
import com.catalog.infrastructure.repository.jpa.VariantTypeJpaRepo;
import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class VariantTypeRepositoryImpl implements VariantTypeRepository {
    private static final Logger log = Loggers.getLogger(VariantTypeRepositoryImpl.class);

    private final VariantTypeJpaAssembler variantTypeJpaAssembler;
    private final VariantTypeJpaRepo repository;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<VariantType> findById(Id typeId) {
        log.debug("Loading variant type by id={}", typeId.getValue());
        return executor.query("VariantType", () -> repository.findByUuidWithOptions(typeId.getValue())
                .map(variantTypeJpaAssembler::toFullDomainGraph));
    }

    @Override
    public void save(VariantType variantType) {
        executor.command("VariantType", () -> {
            log.info("Persisting variant type id={}, name={}", variantType.getId().getValue(), variantType.getName());
            Optional<VariantTypeEntity> existingEntity = repository.findByUuidWithOptions(variantType.getId().getValue());

            VariantTypeEntity entity;
            if(existingEntity.isPresent()) {
                entity =  variantTypeJpaAssembler.buildFullEntityGraph(variantType, existingEntity.get());
            } else {
                entity = variantTypeJpaAssembler.buildFullEntityGraph(variantType, null);
            }

            repository.save(entity);

            List<Event> events = variantType.pullEvents();
            domainEventProducer.produce(variantType.getClass().getSimpleName(), variantType.getId().getValue(), events);
            log.info("Persisted variant type id={}, publishedEvents={}", variantType.getId().getValue(), events.size());
        });
    }
}
