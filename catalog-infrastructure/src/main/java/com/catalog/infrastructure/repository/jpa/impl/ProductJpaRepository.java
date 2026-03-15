package com.catalog.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.support.PersistenceExecutor;
import com.catalog.infrastructure.mapper.jpa.ProductJpaAssembler;
import com.catalog.infrastructure.repository.jpa.ProductJpaRepo;
import lombok.AllArgsConstructor;

import java.util.*;

@AllArgsConstructor
public class ProductJpaRepository implements ProductRepository {
    private static final Logger log = Loggers.getLogger(ProductJpaRepository.class);

    private final ProductJpaAssembler productJpaAssembler;
    private final ProductJpaRepo productJpaRepo;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public void save(Product product) {
        executor.command("Product", () -> {
            log.info("Persisting product id={}, slug={}", product.getId().getValue(), product.getSlug());
            Optional<ProductEntity> productEntity = productJpaRepo.findByUuid(product.getId().getValue());
            ProductEntity entity;

            if (productEntity.isPresent()) {
                entity = productJpaAssembler.buildFullEntityGraph(product, productEntity.get());
            } else {
                entity = productJpaAssembler.buildFullEntityGraph(product, null);
            }

            productJpaRepo.save(entity);

            List<Event> events = product.pullEvents();
            domainEventProducer.produce(product.getClass().getSimpleName(), product.getId().getValue(), events);
            log.info("Persisted product id={}, publishedEvents={}", product.getId().getValue(), events.size());
        });
    }

    @Override
    public void delete(Product product) {
        executor.command("Product", () -> {
            log.info("Deleting product id={}", product.getId().getValue());
            Optional<ProductEntity> productEntity = productJpaRepo.findByUuid(product.getId().getValue());

            if (productEntity.isPresent()) {
                productJpaRepo.delete(productEntity.get());
                List<Event> events = product.pullEvents();
                domainEventProducer.produce(product.getClass().getSimpleName(), product.getId().getValue(), events);
                log.info("Deleted product id={}, publishedEvents={}", product.getId().getValue(), events.size());
            }
        });
    }

    @Override
    public Optional<Product> find(Id productId) {
        log.debug("Loading product by id={}", productId.getValue());
        return executor.query("Product", () -> productJpaRepo.findByUuid(productId.getValue())
                .map(productJpaAssembler::toFullDomainGraph));
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        log.debug("Loading product by slug={}", slug);
        return executor.query("Product", () -> productJpaRepo.findBySlug(slug)
                .map(productJpaAssembler::toFullDomainGraph));
    }

    @Override
    public boolean isSlugTaken(String slug, String excludeProductUuid) {
        log.debug("Checking product slug availability for slug={}, excludeProductUuid={}", slug, excludeProductUuid);
        return executor.query("Product", () -> productJpaRepo.isSlugTaken(slug, excludeProductUuid));
    }

    @Override
    public Optional<Integer> findMaxSlugSuffix(String baseSlug) {
        log.debug("Loading max slug suffix for baseSlug={}", baseSlug);
        return executor.query("Product", () -> productJpaRepo.findMaxSlugSuffix(baseSlug));
    }
}
