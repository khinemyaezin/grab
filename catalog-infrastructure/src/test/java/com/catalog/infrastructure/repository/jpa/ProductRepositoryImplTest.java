package com.catalog.infrastructure.repository.jpa;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.exception.CatalogInfraException;
import com.grab.framework.event.DomainEventProducer;
import com.catalog.infrastructure.mapper.jpa.ProductJpaAssembler;
import com.catalog.infrastructure.repository.jpa.impl.ProductRepositoryImpl;
import com.catalog.infrastructure.repository.jpa.impl.CatalogPersistenceExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductRepositoryImplTest {

    private ProductJpaAssembler productJpaAssembler;
    private ProductJpaRepo productJpaRepo;
    private DomainEventProducer domainEventProducer;
    private ProductRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        productJpaAssembler = mock(ProductJpaAssembler.class);
        productJpaRepo = mock(ProductJpaRepo.class);
        domainEventProducer = mock(DomainEventProducer.class);
        repository = new ProductRepositoryImpl(productJpaAssembler, productJpaRepo, domainEventProducer, new CatalogPersistenceExecutor());
    }

    @Test
    void save_newProduct_createsEntityWithNullExisting() {
        Product product = createProduct();
        ProductEntity newEntity = new ProductEntity();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());
        when(productJpaAssembler.buildFullEntityGraph(product, null)).thenReturn(newEntity);

        repository.save(product);

        verify(productJpaAssembler).buildFullEntityGraph(product, null);
        verify(productJpaRepo).save(newEntity);
    }

    @Test
    void save_existingProduct_mergesWithExistingEntity() {
        Product product = createProduct();
        ProductEntity existingEntity = new ProductEntity();
        existingEntity.setUuid(product.getId().getValue());
        ProductEntity mergedEntity = new ProductEntity();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.of(existingEntity));
        when(productJpaAssembler.buildFullEntityGraph(product, existingEntity)).thenReturn(mergedEntity);

        repository.save(product);

        verify(productJpaAssembler).buildFullEntityGraph(product, existingEntity);
        verify(productJpaRepo).save(mergedEntity);
    }

    @Test
    void save_publishesDomainEvents() {
        Product product = createProduct();
        product.changeCategory(id("new-category"));

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());
        when(productJpaAssembler.buildFullEntityGraph(eq(product), isNull())).thenReturn(new ProductEntity());

        repository.save(product);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Event>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventProducer).produce(eq("Product"), eq(product.getId().getValue()), eventsCaptor.capture());
        assertFalse(eventsCaptor.getValue().isEmpty());
        assertTrue(product.getEvents().isEmpty());
    }

    @Test
    void save_productWithNoEvents_skipsPublishing() {
        Product product = createProduct();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());
        when(productJpaAssembler.buildFullEntityGraph(eq(product), isNull())).thenReturn(new ProductEntity());

        repository.save(product);

        verify(domainEventProducer).produce(anyString(), anyString(), anyList());
    }

    @Test
    void save_callsPersistAfterAssembly() {
        Product product = createProduct();
        ProductEntity entity = new ProductEntity();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());
        when(productJpaAssembler.buildFullEntityGraph(product, null)).thenReturn(entity);

        repository.save(product);

        var inOrder = inOrder(productJpaAssembler, productJpaRepo);
        inOrder.verify(productJpaAssembler).buildFullEntityGraph(product, null);
        inOrder.verify(productJpaRepo).save(entity);
        verify(domainEventProducer).produce(anyString(), anyString(), anyList());
        assertTrue(product.getEvents().isEmpty());
    }

    @Test
    void save_whenConstraintViolationOccurs_shouldThrowTypedInfrastructureConflict() {
        Product product = createProduct();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());
        when(productJpaAssembler.buildFullEntityGraph(eq(product), isNull())).thenReturn(new ProductEntity());
        when(productJpaRepo.save(any(ProductEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value"));

        CatalogInfraException exception = assertThrows(CatalogInfraException.class, () -> repository.save(product));
        assertEquals("cat.infra.persistence.conflict", exception.getMessageSource().code());
        assertEquals("Product", exception.getMessageSource().args().get("resource"));
    }

    @Test
    void delete_existingProduct_deletesAndPublishesEvents() {
        Product product = createProduct();
        product.delete();
        ProductEntity existingEntity = new ProductEntity();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.of(existingEntity));

        repository.delete(product);

        verify(productJpaRepo).delete(existingEntity);
        verify(domainEventProducer).produce(eq("Product"), eq(product.getId().getValue()), anyList());
        assertTrue(product.getEvents().isEmpty());
    }

    @Test
    void delete_nonExistingProduct_doesNothing() {
        Product product = createProduct();

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.empty());

        repository.delete(product);

        verify(productJpaRepo, never()).delete((ProductEntity) any());
        verify(domainEventProducer, never()).produce(anyString(), anyString(), anyList());
    }

    private Product createProduct() {
        Product product = Product.create(id("product-1"), "T-Shirt", id("clothing"));

        ProductVariant variant = ProductVariant.create(
                id("v1"),
                "SKU-001",
                List.of(
                        new ProductVariation(id("red"), id("color")),
                        new ProductVariation(id("small"), id("size"))
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
