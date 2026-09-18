package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.domain.repository.ProductPublicationRepository;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.catalog.infrastructure.mapper.jpa.ProductPublicationJpaAssembler;
import com.catalog.infrastructure.repository.jpa.ProductJpaRepo;
import com.catalog.infrastructure.repository.jpa.ProductPublicationJpaRepository;
import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultProductPublicationRepository implements ProductPublicationRepository {

    private final ProductPublicationJpaRepository jpaRepository;
    private final ProductJpaRepo productJpaRepo;
    private final ProductPublicationJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<ProductPublication> find(Id productId, Id salesChannelId) {
        return executor.query("ProductPublication", () -> {
            Optional<ProductEntity> product = productJpaRepo.findByUuid(productId.getValue());
            if (product.isEmpty()) {
                return Optional.empty();
            }
            ProductEntity productEntity = product.get();
            Optional<ProductPublicationEntity> entity = jpaRepository.findByProductIdAndSalesChannelId(
                    productEntity.getId(),
                    salesChannelId.getValue());
            return entity.map(publicationEntity -> mapper.toFullDomainGraph(publicationEntity, productEntity));
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
                    .map(entity -> mapper.toFullDomainGraph(entity, productEntity))
                    .toList();
        });
    }

    @Override
    public boolean exists(Id productId, Id salesChannelId) {
        return executor.query("ProductPublication", () -> {
            Optional<ProductEntity> product = productJpaRepo.findByUuid(productId.getValue());
            if (product.isEmpty()) {
                return false;
            }
            ProductEntity productEntity = product.get();
            return jpaRepository.existsByProductIdAndSalesChannelId(
                    productEntity.getId(),
                    salesChannelId.getValue());
        });
    }

    @Override
    public void save(ProductPublication publication) {
        executor.command("ProductPublication", () -> {
            ProductEntity product = productJpaRepo.findByUuid(publication.getProductId().getValue())
                    .orElseThrow(() -> new IllegalStateException("Product not found for publication"));
            Optional<ProductPublicationEntity> existingEntity = jpaRepository.findByProductIdAndSalesChannelId(
                    product.getId(),
                    publication.getSalesChannelId().getValue());
            ProductPublicationEntity entity = mapper.buildFullEntityGraph(
                    publication,
                    existingEntity.orElse(null),
                    product);
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
            Optional<ProductEntity> product = productJpaRepo.findByUuid(publication.getProductId().getValue());
            product.ifPresent(productEntity -> {
                Optional<ProductPublicationEntity> entity = jpaRepository.findByProductIdAndSalesChannelId(
                        productEntity.getId(),
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
}
