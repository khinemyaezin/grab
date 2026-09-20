package com.catalog.adapter.persistence.adapter;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.domain.port.outbound.ProductPublicationRepository;
import com.catalog.adapter.persistence.entity.ProductEntity;
import com.catalog.adapter.persistence.entity.ProductPublicationEntity;
import com.catalog.adapter.persistence.entity.ProductVariantEntity;
import com.catalog.adapter.persistence.mapper.ProductPublicationJpaAssembler;
import com.catalog.adapter.persistence.repository.ProductJpaRepo;
import com.catalog.adapter.persistence.repository.ProductPublicationJpaRepository;
import com.catalog.adapter.persistence.repository.ProductVariantJpaRepo;
import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ProductPublicationRepositoryAdapter implements ProductPublicationRepository {

    private final ProductPublicationJpaRepository jpaRepository;
    private final ProductVariantJpaRepo variantJpaRepo;
    private final ProductJpaRepo productJpaRepo;
    private final ProductPublicationJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<ProductPublication> find(Id variantId, Id salesChannelId) {
        return executor.query("ProductPublication", () -> {
            Optional<ProductVariantEntity> variant = variantJpaRepo.findByUuid(variantId.getValue());
            if (variant.isEmpty()) {
                return Optional.empty();
            }
            ProductVariantEntity variantEntity = variant.get();
            Optional<ProductPublicationEntity> entity = jpaRepository.findByVariantIdAndSalesChannelId(
                    variantEntity.getId(),
                    salesChannelId.getValue());
            return entity.map(publicationEntity -> mapper.toFullDomainGraph(publicationEntity, variantEntity));
        });
    }

    @Override
    public List<ProductPublication> findByVariantId(Id variantId) {
        return executor.query("ProductPublication", () -> {
            Optional<ProductVariantEntity> variant = variantJpaRepo.findByUuid(variantId.getValue());
            if (variant.isEmpty()) {
                return List.of();
            }
            ProductVariantEntity variantEntity = variant.get();
            List<ProductPublicationEntity> entities = jpaRepository.findByVariantId(variantEntity.getId());
            return entities.stream()
                    .map(entity -> mapper.toFullDomainGraph(entity, variantEntity))
                    .toList();
        });
    }

    @Override
    public List<ProductPublication> findByProductId(Id productId) {
        return executor.query("ProductPublication", () -> {
            Optional<ProductEntity> product = productJpaRepo.findByUuid(productId.getValue());
            if (product.isEmpty()) {
                return List.of();
            }
            ProductEntity productEntity = product.get();
            List<ProductPublicationEntity> entities = jpaRepository.findByProductId(productEntity.getId());
            return entities.stream()
                    .map(this::toDomain)
                    .toList();
        });
    }

    @Override
    public boolean exists(Id variantId, Id salesChannelId) {
        return executor.query("ProductPublication", () -> {
            Optional<ProductVariantEntity> variant = variantJpaRepo.findByUuid(variantId.getValue());
            if (variant.isEmpty()) {
                return false;
            }
            ProductVariantEntity variantEntity = variant.get();
            return jpaRepository.existsByVariantIdAndSalesChannelId(
                    variantEntity.getId(),
                    salesChannelId.getValue());
        });
    }

    @Override
    public void save(ProductPublication publication) {
        executor.command("ProductPublication", () -> {
            ProductVariantEntity variant = variantJpaRepo.findByUuid(publication.getVariantId().getValue())
                    .orElseThrow(() -> new IllegalStateException("Variant not found for publication"));
            Optional<ProductPublicationEntity> existingEntity = jpaRepository.findByVariantIdAndSalesChannelId(
                    variant.getId(),
                    publication.getSalesChannelId().getValue());
            ProductPublicationEntity entity = mapper.buildFullEntityGraph(
                    publication,
                    existingEntity.orElse(null),
                    variant);
            jpaRepository.save(entity);
            List<Event> events = publication.pullEvents();
            domainEventProducer.produce(
                    publication.getClass().getSimpleName(),
                    publication.getId().getValue(),
                    events);
            return null;
        });
    }

    @Override
    public void delete(ProductPublication publication) {
        executor.command("ProductPublication", () -> {
            Optional<ProductVariantEntity> variant = variantJpaRepo.findByUuid(publication.getVariantId().getValue());
            variant.ifPresent(variantEntity -> {
                Optional<ProductPublicationEntity> entity = jpaRepository.findByVariantIdAndSalesChannelId(
                        variantEntity.getId(),
                        publication.getSalesChannelId().getValue());
                entity.ifPresent(jpaRepository::delete);
            });
            List<Event> events = publication.pullEvents();
            domainEventProducer.produce(
                    publication.getClass().getSimpleName(),
                    publication.getId().getValue(),
                    events);
            return null;
        });
    }

    private ProductPublication toDomain(ProductPublicationEntity entity) {
        ProductVariantEntity variant = variantJpaRepo.findById(entity.getVariantId())
                .orElseThrow(() -> new IllegalStateException("Variant not found for publication"));
        return mapper.toFullDomainGraph(entity, variant);
    }
}
