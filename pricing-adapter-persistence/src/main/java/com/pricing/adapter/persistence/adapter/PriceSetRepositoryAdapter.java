package com.pricing.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.pricing.adapter.persistence.entity.PriceSetEntity;
import com.pricing.adapter.persistence.mapper.jpa.impl.PricingJpaAssembler;
import com.pricing.adapter.persistence.repository.jpa.PriceSetJpaRepository;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.port.outbound.PriceSetRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class PriceSetRepositoryAdapter implements PriceSetRepository {
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
