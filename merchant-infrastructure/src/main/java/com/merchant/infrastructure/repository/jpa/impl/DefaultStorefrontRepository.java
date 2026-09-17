package com.merchant.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.repository.StorefrontRepository;
import com.merchant.domain.valueobject.StorefrontSlug;
import com.merchant.infrastructure.entity.StorefrontEntity;
import com.merchant.infrastructure.mapper.jpa.impl.StorefrontJpaAssembler;
import com.merchant.infrastructure.repository.jpa.StorefrontJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultStorefrontRepository implements StorefrontRepository {
    private final StorefrontJpaRepository storefronts;
    private final StorefrontJpaAssembler assembler;
    private final DomainEventProducer events;
    private final PersistenceExecutor executor;

    @Override
    public Optional<Storefront> findById(Id id) {
        return executor.query("Storefront", () ->
                storefronts.findByUuid(id.getValue()).map(assembler::toDomain));
    }

    @Override
    public List<Storefront> findByMerchantId(Id merchantId) {
        return executor.query("Storefront", () ->
                storefronts.findByMerchantIdOrderByCreatedAtDesc(merchantId.getValue())
                        .stream()
                        .map(assembler::toDomain)
                        .toList());
    }

    @Override
    public boolean existsSlug(StorefrontSlug slug, Id excludingId) {
        return executor.query("Storefront", () -> {
            if (excludingId == null) {
                return storefronts.existsBySlug(slug.value());
            }
            return storefronts.existsBySlugExcludingUuid(slug.value(), excludingId.getValue());
        });
    }

    @Override
    public Storefront save(Storefront storefront) {
        return executor.command("Storefront", () -> {
            StorefrontEntity existing = storefronts.findByUuid(storefront.getId().getValue()).orElse(null);
            StorefrontEntity saved = storefronts.save(assembler.toEntity(storefront, existing));
            List<Event> pending = storefront.pullEvents();
            events.produce("Storefront", storefront.getId().getValue(), pending);
            return assembler.toDomain(saved);
        });
    }
}
