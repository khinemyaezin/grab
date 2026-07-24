package com.pricing.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.pricing.domain.aggregate.PricePreference;
import com.pricing.domain.repository.PricePreferenceRepository;
import com.pricing.infrastructure.entity.PricePreferenceEntity;
import com.pricing.infrastructure.mapper.jpa.impl.PricingJpaAssembler;
import com.pricing.infrastructure.repository.jpa.PricePreferenceJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultPricePreferenceRepository implements PricePreferenceRepository {
    private final PricePreferenceJpaRepository preferences;
    private final PricingJpaAssembler assembler;
    private final DomainEventProducer events;
    private final PersistenceExecutor executor;

    @Override
    public Optional<PricePreference> findById(Id id) {
        return executor.query("PricePreference", () ->
                preferences.findByUuid(id.getValue()).map(assembler::toDomain));
    }

    @Override
    public List<PricePreference> findAll() {
        return executor.query("PricePreference", () ->
                preferences.findAll().stream().map(assembler::toDomain).toList());
    }

    @Override
    public PricePreference save(PricePreference preference) {
        return executor.command("PricePreference", () -> {
            PricePreferenceEntity existing = preferences.findByUuid(preference.getId().getValue()).orElse(null);
            PricePreferenceEntity saved = preferences.save(assembler.toEntity(preference, existing));
            List<Event> pending = preference.pullEvents();
            events.produce("PricePreference", preference.getId().getValue(), pending);
            return assembler.toDomain(saved);
        });
    }

    @Override
    public void delete(Id id) {
        executor.command("PricePreference", () -> preferences.deleteByUuid(id.getValue()));
    }
}
