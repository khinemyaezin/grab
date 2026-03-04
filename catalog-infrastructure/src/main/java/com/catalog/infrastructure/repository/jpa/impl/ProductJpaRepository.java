package com.catalog.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.grab.framework.event.DomainEventProducer;
import com.catalog.infrastructure.mapper.jpa.ProductJpaAssembler;
import com.catalog.infrastructure.repository.jpa.ProductJpaRepo;
import lombok.AllArgsConstructor;

import java.util.*;

@AllArgsConstructor
public class ProductJpaRepository implements ProductRepository {
    private final ProductJpaAssembler productJpaAssembler;
    private final ProductJpaRepo productJpaRepo;
    private final DomainEventProducer domainEventProducer;

    @Override
    public void save(Product product) {
        Optional<ProductEntity> productEntity = productJpaRepo.findByUuid(product.getId().getValue());
        ProductEntity entity;

        if(productEntity.isPresent()) {
            entity = productJpaAssembler.buildFullEntityGraph(product, productEntity.get());
        } else {
            entity = productJpaAssembler.buildFullEntityGraph(product, null);
        }

        productJpaRepo.save(entity);
        domainEventProducer.produce(product.getEvents());
    }

    @Override
    public void delete(Product product) {
        Optional<ProductEntity> productEntity = this.productJpaRepo.findByUuid(product.getId().getValue());
        if(productEntity.isPresent()) {
            productJpaRepo.delete(productEntity.get());
            domainEventProducer.produce(product.getEvents());
        }
    }

    @Override
    public Optional<Product> find(Id productId) {
        Optional<ProductEntity> productEntity = this.productJpaRepo.findByUuid(productId.getValue());
        if(productEntity.isEmpty()) {
            return Optional.empty();
        }

        ProductEntity entity = productEntity.get();
        Product product = productJpaAssembler.toFullDomainGraph(entity);
        return Optional.of(product);
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        Optional<ProductEntity> productEntity = this.productJpaRepo.findBySlug(slug);
        if(productEntity.isEmpty()) {
            return Optional.empty();
        }

        ProductEntity entity = productEntity.get();
        Product product = productJpaAssembler.toFullDomainGraph(entity);
        return Optional.of(product);
    }

    @Override
    public boolean isSlugTaken(String slug, String excludeProductUuid) {
        return productJpaRepo.isSlugTaken(slug, excludeProductUuid);
    }

    @Override
    public Optional<Integer> findMaxSlugSuffix(String baseSlug) {
        return productJpaRepo.findMaxSlugSuffix(baseSlug);
    }
}
