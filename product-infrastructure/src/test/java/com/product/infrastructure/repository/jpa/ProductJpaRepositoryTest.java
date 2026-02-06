package com.product.infrastructure.repository.jpa;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.ProductVariantStatus;
import com.product.domain.valueobject.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.event.DomainEventProducer;
import com.product.infrastructure.mapper.jpa.ProductJpaAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductJpaRepositoryTest {

    private ProductJpaAssembler productJpaAssembler;
    private ProductJpaRepo productJpaRepo;
    private DomainEventProducer domainEventProducer;
    private ProductJpaRepository repository;

    @BeforeEach
    void setUp() {
        productJpaAssembler = mock(ProductJpaAssembler.class);
        productJpaRepo = mock(ProductJpaRepo.class);
        domainEventProducer = mock(DomainEventProducer.class);
        repository = new ProductJpaRepository(productJpaAssembler, productJpaRepo, domainEventProducer);
    }

    @Test
    void save_newProduct_createsEntityWithNullExisting() {
        Product product = createProduct();
        ProductEntity newEntity = new ProductEntity();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());
        when(productJpaAssembler.toFullEntityGraph(product, null)).thenReturn(newEntity);

        repository.save(product);

        verify(productJpaAssembler).toFullEntityGraph(product, null);
        verify(productJpaRepo).save(newEntity);
    }

    @Test
    void save_existingProduct_mergesWithExistingEntity() {
        Product product = createProduct();
        ProductEntity existingEntity = new ProductEntity();
        existingEntity.setUuid(product.getId().getValue());
        ProductEntity mergedEntity = new ProductEntity();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.of(existingEntity));
        when(productJpaAssembler.toFullEntityGraph(product, existingEntity)).thenReturn(mergedEntity);

        repository.save(product);

        verify(productJpaAssembler).toFullEntityGraph(product, existingEntity);
        verify(productJpaRepo).save(mergedEntity);
    }

    @Test
    void save_publishesDomainEvents() {
        Product product = createProduct();
        product.changeCategory(id("new-category"));

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());
        when(productJpaAssembler.toFullEntityGraph(eq(product), isNull())).thenReturn(new ProductEntity());

        repository.save(product);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Event>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventProducer).produce(eventsCaptor.capture());
        assertFalse(eventsCaptor.getValue().isEmpty());
    }

    @Test
    void save_productWithNoEvents_publishesEmptyList() {
        Product product = createProduct();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());
        when(productJpaAssembler.toFullEntityGraph(eq(product), isNull())).thenReturn(new ProductEntity());

        repository.save(product);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Event>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventProducer).produce(eventsCaptor.capture());
        assertTrue(eventsCaptor.getValue().isEmpty());
    }

    @Test
    void save_callsPersistAfterAssembly() {
        Product product = createProduct();
        ProductEntity entity = new ProductEntity();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());
        when(productJpaAssembler.toFullEntityGraph(product, null)).thenReturn(entity);

        repository.save(product);

        var inOrder = inOrder(productJpaAssembler, productJpaRepo, domainEventProducer);
        inOrder.verify(productJpaAssembler).toFullEntityGraph(product, null);
        inOrder.verify(productJpaRepo).save(entity);
        inOrder.verify(domainEventProducer).produce(product.getEvents());
    }

    @Test
    void delete_existingProduct_deletesAndPublishesEvents() {
        Product product = createProduct();
        product.delete();
        ProductEntity existingEntity = new ProductEntity();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.of(existingEntity));

        repository.delete(product);

        verify(productJpaRepo).delete(existingEntity);
        verify(domainEventProducer).produce(product.getEvents());
    }

    @Test
    void delete_nonExistingProduct_doesNothing() {
        Product product = createProduct();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());

        repository.delete(product);

        verify(productJpaRepo, never()).delete(any());
        verify(domainEventProducer, never()).produce(anyList());
    }

    private Product createProduct() {
        Product product = Product.create(id("product-1"), "T-Shirt", id("clothing"));

        ProductVariant variant = ProductVariant.create(
                id("v1"),
                "SKU-001",
                List.of(
                        new ProductVariation("Red", id("red"), "Color", id("color")),
                        new ProductVariation("Small", id("small"), "Size", id("size"))
                )
        );
        product.addVariant(variant);
        return product;
    }

    private static Id id(String value) {
        return new Id() {
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Id other)) return false;
                return Objects.equals(value, other.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(value);
            }

            @Override
            public String toString() {
                return value;
            }
        };
    }
}