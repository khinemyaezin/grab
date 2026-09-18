package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.catalog.infrastructure.mapper.jpa.ProductPublicationJpaAssembler;
import com.catalog.infrastructure.repository.jpa.ProductJpaRepo;
import com.catalog.infrastructure.repository.jpa.ProductPublicationJpaRepository;
import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.support.PersistenceExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefaultProductPublicationRepositoryTest {

    private static final String RESOURCE = "ProductPublication";

    private ProductPublicationJpaRepository jpaRepository;
    private ProductJpaRepo productJpaRepo;
    private ProductPublicationJpaAssembler mapper;
    private DomainEventProducer domainEventProducer;
    private PersistenceExecutor executor;
    private DefaultProductPublicationRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(ProductPublicationJpaRepository.class);
        productJpaRepo = mock(ProductJpaRepo.class);
        mapper = mock(ProductPublicationJpaAssembler.class);
        domainEventProducer = mock(DomainEventProducer.class);
        executor = mock(PersistenceExecutor.class);
        repository = new DefaultProductPublicationRepository(
                jpaRepository,
                productJpaRepo,
                mapper,
                domainEventProducer,
                executor
        );

        when(executor.query(eq(RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
        when(executor.command(eq(RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
    }

    @Test
    void find_returnsPublication_whenExists() {
        ProductEntity product = productEntity(20L, "p1");
        ProductPublicationEntity entity = publicationEntity(20L, "channel-1");
        ProductPublication publication = ProductPublication.restore(id("p1"), id("channel-1"), null);
        when(productJpaRepo.findByUuid("p1")).thenReturn(Optional.of(product));
        when(jpaRepository.findByProductIdAndSalesChannelId(20L, "channel-1")).thenReturn(Optional.of(entity));
        when(mapper.toFullDomainGraph(entity, product)).thenReturn(publication);

        Optional<ProductPublication> result = repository.find(id("p1"), id("channel-1"));

        assertTrue(result.isPresent());
        assertSame(publication, result.get());
        verify(mapper).toFullDomainGraph(entity, product);
        verify(executor).query(eq(RESOURCE), any(Supplier.class));
    }

    @Test
    void find_returnsEmpty_whenProductMissing() {
        when(productJpaRepo.findByUuid("p1")).thenReturn(Optional.empty());

        Optional<ProductPublication> result = repository.find(id("p1"), id("channel-1"));

        assertTrue(result.isEmpty());
        verifyNoInteractions(jpaRepository, mapper);
    }

    @Test
    void findByProductId_mapsEntitiesThroughAssembler() {
        ProductEntity product = productEntity(20L, "p1");
        ProductPublicationEntity entity = publicationEntity(20L, "channel-1");
        ProductPublication publication = ProductPublication.restore(id("p1"), id("channel-1"), null);
        when(productJpaRepo.findByUuid("p1")).thenReturn(Optional.of(product));
        when(jpaRepository.findByProductId(20L)).thenReturn(List.of(entity));
        when(mapper.toFullDomainGraph(entity, product)).thenReturn(publication);

        List<ProductPublication> result = repository.findByProductId(id("p1"));

        assertEquals(1, result.size());
        assertSame(publication, result.get(0));
        verify(mapper).toFullDomainGraph(entity, product);
    }

    @Test
    void save_createsNewEntity_whenNotExisting() {
        Instant publishedAt = Instant.parse("2026-09-17T00:00:00Z");
        ProductPublication publication = ProductPublication.publish(id("p1"), id("channel-1"), publishedAt);
        ProductEntity product = productEntity(20L, "p1");
        ProductPublicationEntity newEntity = publicationEntity(20L, "channel-1");
        when(productJpaRepo.findByUuid("p1")).thenReturn(Optional.of(product));
        when(jpaRepository.findByProductIdAndSalesChannelId(20L, "channel-1")).thenReturn(Optional.empty());
        when(mapper.buildFullEntityGraph(same(publication), isNull(), same(product))).thenReturn(newEntity);

        repository.save(publication);

        verify(mapper).buildFullEntityGraph(same(publication), isNull(), same(product));
        verify(jpaRepository).save(newEntity);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Event>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventProducer).produce(eq("ProductPublication"), eq("p1:channel-1"), eventsCaptor.capture());
        assertFalse(eventsCaptor.getValue().isEmpty());
    }

    @Test
    void save_updatesExistingEntity_whenAlreadyExists() {
        Instant publishedAt = Instant.parse("2026-09-17T00:00:00Z");
        ProductPublication publication = ProductPublication.publish(id("p1"), id("channel-1"), publishedAt);
        ProductEntity product = productEntity(20L, "p1");
        ProductPublicationEntity existingEntity = publicationEntity(20L, "channel-1");
        ProductPublicationEntity mergedEntity = publicationEntity(20L, "channel-1");
        when(productJpaRepo.findByUuid("p1")).thenReturn(Optional.of(product));
        when(jpaRepository.findByProductIdAndSalesChannelId(20L, "channel-1")).thenReturn(Optional.of(existingEntity));
        when(mapper.buildFullEntityGraph(same(publication), same(existingEntity), same(product))).thenReturn(mergedEntity);

        repository.save(publication);

        verify(mapper).buildFullEntityGraph(same(publication), same(existingEntity), same(product));
        verify(jpaRepository).save(mergedEntity);
        verify(domainEventProducer).produce(eq("ProductPublication"), eq("p1:channel-1"), any());
    }

    @Test
    void delete_removesExistingEntity_whenFound() {
        ProductPublication publication = ProductPublication.restore(id("p1"), id("channel-1"), null);
        publication.unpublish();
        ProductEntity product = productEntity(20L, "p1");
        ProductPublicationEntity entity = publicationEntity(20L, "channel-1");
        when(productJpaRepo.findByUuid("p1")).thenReturn(Optional.of(product));
        when(jpaRepository.findByProductIdAndSalesChannelId(20L, "channel-1")).thenReturn(Optional.of(entity));

        repository.delete(publication);

        verify(jpaRepository).delete(entity);
        verifyNoInteractions(mapper);
        verify(domainEventProducer).produce(eq("ProductPublication"), eq("p1:channel-1"), any());
    }

    @Test
    void delete_doesNothing_whenNotExisting() {
        ProductPublication publication = ProductPublication.restore(id("p1"), id("channel-1"), null);
        when(productJpaRepo.findByUuid("p1")).thenReturn(Optional.empty());

        repository.delete(publication);

        verify(jpaRepository, never()).delete(any());
        verifyNoInteractions(mapper);
        verify(domainEventProducer).produce(eq("ProductPublication"), eq("p1:channel-1"), any());
    }

    private static Id id(String value) {
        return new CommonId(value);
    }

    private static ProductEntity productEntity(Long id, String uuid) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setUuid(uuid);
        return product;
    }

    private static ProductPublicationEntity publicationEntity(Long productId, String salesChannelId) {
        ProductPublicationEntity entity = new ProductPublicationEntity();
        entity.setProductId(productId);
        entity.setSalesChannelId(salesChannelId);
        return entity;
    }
}
