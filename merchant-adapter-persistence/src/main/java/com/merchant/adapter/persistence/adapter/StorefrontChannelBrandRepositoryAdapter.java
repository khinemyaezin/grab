package com.merchant.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.merchant.adapter.persistence.entity.StorefrontChannelBrandEntity;
import com.merchant.adapter.persistence.entity.StorefrontEntity;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontChannelBrandJpaAssembler;
import com.merchant.adapter.persistence.repository.jpa.StorefrontChannelBrandJpaRepository;
import com.merchant.adapter.persistence.repository.jpa.StorefrontJpaRepository;
import com.merchant.domain.aggregate.StorefrontChannelBrand;
import com.merchant.domain.port.outbound.StorefrontChannelBrandRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class StorefrontChannelBrandRepositoryAdapter implements StorefrontChannelBrandRepository {
    private final StorefrontChannelBrandJpaRepository jpaRepository;
    private final StorefrontJpaRepository storefronts;
    private final StorefrontChannelBrandJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<StorefrontChannelBrand> findByStorefrontId(Id storefrontId) {
        return executor.query("StorefrontChannelBrand", () -> {
            Optional<StorefrontEntity> storefront = storefronts.findByUuid(storefrontId.getValue());
            if (storefront.isEmpty()) {
                return Optional.empty();
            }
            StorefrontEntity storefrontEntity = storefront.get();
            Optional<StorefrontChannelBrandEntity> entity = jpaRepository.findByStorefrontId(storefrontEntity.getId());
            return entity.map(brandEntity -> mapper.toFullDomainGraph(brandEntity, storefrontEntity));
        });
    }

    @Override
    public boolean existsByStorefrontId(Id storefrontId) {
        return executor.query("StorefrontChannelBrand", () -> {
            Optional<StorefrontEntity> storefront = storefronts.findByUuid(storefrontId.getValue());
            if (storefront.isEmpty()) {
                return false;
            }
            StorefrontEntity storefrontEntity = storefront.get();
            return jpaRepository.existsByStorefrontId(storefrontEntity.getId());
        });
    }

    @Override
    public void save(StorefrontChannelBrand brand) {
        executor.command("StorefrontChannelBrand", () -> {
            StorefrontEntity storefront = storefronts.findByUuid(brand.getStorefrontId().getValue())
                    .orElseThrow(() -> new IllegalStateException("Storefront not found for channel brand"));
            Optional<StorefrontChannelBrandEntity> existingEntity = jpaRepository.findByStorefrontId(storefront.getId());
            StorefrontChannelBrandEntity entity = mapper.buildFullEntityGraph(
                    brand,
                    existingEntity.orElse(null),
                    storefront);
            jpaRepository.save(entity);
            List<Event> events = brand.pullEvents();
            domainEventProducer.produce(
                    brand.getClass().getSimpleName(),
                    brand.getId().getValue(),
                    events);
            return null;
        });
    }

    @Override
    public void delete(StorefrontChannelBrand brand) {
        executor.command("StorefrontChannelBrand", () -> {
            Optional<StorefrontEntity> storefront = storefronts.findByUuid(brand.getStorefrontId().getValue());
            storefront.ifPresent(storefrontEntity -> {
                Optional<StorefrontChannelBrandEntity> entity =
                        jpaRepository.findByStorefrontId(storefrontEntity.getId());
                entity.ifPresent(jpaRepository::delete);
            });
            List<Event> events = brand.pullEvents();
            domainEventProducer.produce(
                    brand.getClass().getSimpleName(),
                    brand.getId().getValue(),
                    events);
            return null;
        });
    }
}
