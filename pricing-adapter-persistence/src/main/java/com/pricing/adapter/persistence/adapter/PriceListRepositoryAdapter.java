package com.pricing.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.pricing.adapter.persistence.entity.PriceListEntity;
import com.pricing.adapter.persistence.entity.PriceSetEntity;
import com.pricing.adapter.persistence.mapper.jpa.impl.PricingJpaAssembler;
import com.pricing.adapter.persistence.repository.jpa.PriceListJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PriceSetJpaRepository;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.port.outbound.PriceListRepository;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class PriceListRepositoryAdapter implements PriceListRepository {
    private final PriceListJpaRepository priceLists;
    private final PriceSetJpaRepository priceSets;
    private final PricingJpaAssembler assembler;
    private final DomainEventProducer events;
    private final PersistenceExecutor executor;

    @Override
    public Optional<PriceList> findById(Id id) {
        return executor.query("PriceList", () ->
                priceLists.findByUuid(id.getValue()).map(assembler::toDomain));
    }

    @Override
    public List<PriceList> findAll() {
        return executor.query("PriceList", () ->
                priceLists.findAll().stream().map(assembler::toDomain).toList());
    }

    @Override
    public PriceList save(PriceList priceList) {
        return executor.command("PriceList", () -> {
            PriceListEntity existing = priceLists.findByUuid(priceList.getId().getValue()).orElse(null);
            Map<String, PriceSetEntity> cache = new HashMap<>();
            PriceListEntity saved = priceLists.save(assembler.toEntity(priceList, existing, priceSetId ->
                    cache.computeIfAbsent(priceSetId, uuid -> priceSets.findByUuid(uuid)
                            .orElseThrow(() -> new IllegalArgumentException("Price set not found: " + uuid)))));
            List<Event> pending = priceList.pullEvents();
            events.produce("PriceList", priceList.getId().getValue(), pending);
            return assembler.toDomain(saved);
        });
    }

    @Override
    public void delete(Id id) {
        executor.command("PriceList", () -> priceLists.deleteByUuid(id.getValue()));
    }
}
