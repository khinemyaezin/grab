package com.product.infrastructure.repository.jpa;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.repository.ProductRepository;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.event.DomainEventProducer;
import com.product.infrastructure.mapper.jpa.ProductJpaAssembler;
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
            entity = productJpaAssembler.toFullEntityGraph(product, productEntity.get());
        } else {
            entity = productJpaAssembler.toFullEntityGraph(product, null);
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
    public Product find(Id productId) {
        Optional<ProductEntity> productEntity = this.productJpaRepo.findByUuid(productId.getValue());
        if(productEntity.isEmpty()) {
            return null;
        }

        ProductEntity entity = productEntity.get();
        return productJpaAssembler.toFullDomainGraph(entity);
    }
}
