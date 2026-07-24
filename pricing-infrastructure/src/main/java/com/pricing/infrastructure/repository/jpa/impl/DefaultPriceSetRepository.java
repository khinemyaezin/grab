package com.pricing.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.repository.PriceSetRepository;
import com.pricing.infrastructure.entity.PriceSetEntity;
import com.pricing.infrastructure.mapper.jpa.impl.PricingJpaAssembler;
import com.pricing.infrastructure.repository.jpa.PriceSetJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultPriceSetRepository implements PriceSetRepository {
    private final PriceSetJpaRepository priceSets;
    private final PricingJpaAssembler assembler;
    private final DomainEventProducer events;
    private final PersistenceExecutor executor;

    @Override
    public Optional<PriceSet> findById(Id id) {
        return executor.query("PriceSet", () ->
                priceSets.findByUuid(id.getValue()).map(assembler::toDomain));
    }

    @Override
    public PriceSet save(PriceSet priceSet) {
        return executor.command("PriceSet", () -> {
            PriceSetEntity existing = priceSets.findByUuid(priceSet.getId().getValue()).orElse(null);
            PriceSetEntity saved = priceSets.save(assembler.toEntity(priceSet, existing));
            List<Event> pending = priceSet.pullEvents();
            events.produce("PriceSet", priceSet.getId().getValue(), pending);
            return assembler.toDomain(saved);
        });
    }

    @Override
    public void delete(Id id) {
        executor.command("PriceSet", () -> priceSets.deleteByUuid(id.getValue()));
    }
}
