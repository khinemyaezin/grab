package com.catalog.infrastructure.repository.jpa;

import com.catalog.domain.aggregate.VariantOption;
import com.catalog.domain.aggregate.VariantType;
import com.catalog.infrastructure.entity.entity.VariantTypeEntity;
import com.catalog.infrastructure.mapper.jpa.VariantTypeJpaAssembler;
import com.catalog.infrastructure.repository.jpa.impl.CatalogPersistenceExecutor;
import com.catalog.infrastructure.repository.jpa.impl.VariantTypeRepositoryImpl;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VariantTypeRepositoryImplTest {

    private VariantTypeJpaAssembler variantTypeJpaAssembler;
    private VariantTypeJpaRepo variantTypeJpaRepo;
    private DomainEventProducer domainEventProducer;
    private VariantTypeRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        variantTypeJpaAssembler = mock(VariantTypeJpaAssembler.class);
        variantTypeJpaRepo = mock(VariantTypeJpaRepo.class);
        domainEventProducer = mock(DomainEventProducer.class);
        repository = new VariantTypeRepositoryImpl(
                variantTypeJpaAssembler, variantTypeJpaRepo,
                domainEventProducer, new CatalogPersistenceExecutor());
    }

    @Test
    void save_newVariantType_createsEntityWithNullExisting() {
        VariantType variantType = createVariantType();
        VariantTypeEntity newEntity = new VariantTypeEntity();

        when(variantTypeJpaRepo.findByUuidWithOptions(variantType.getId().getValue())).thenReturn(Optional.empty());
        when(variantTypeJpaAssembler.buildFullEntityGraph(variantType, null)).thenReturn(newEntity);

        repository.save(variantType);

        verify(variantTypeJpaAssembler).buildFullEntityGraph(variantType, null);
        verify(variantTypeJpaRepo).save(newEntity);
        verify(domainEventProducer).produce(eq("VariantType"), eq(variantType.getId().getValue()), anyList());
    }

    @Test
    void save_existingVariantType_mergesWithExistingEntity() {
        VariantType variantType = createVariantType();
        VariantTypeEntity existingEntity = new VariantTypeEntity();
        VariantTypeEntity mergedEntity = new VariantTypeEntity();

        when(variantTypeJpaRepo.findByUuidWithOptions(variantType.getId().getValue())).thenReturn(Optional.of(existingEntity));
        when(variantTypeJpaAssembler.buildFullEntityGraph(variantType, existingEntity)).thenReturn(mergedEntity);

        repository.save(variantType);

        verify(variantTypeJpaAssembler).buildFullEntityGraph(variantType, existingEntity);
        verify(variantTypeJpaRepo).save(mergedEntity);
    }

    @Test
    void save_whenConstraintViolationOccurs_shouldThrowTypedInfrastructureConflict() {
        VariantType variantType = createVariantType();

        when(variantTypeJpaRepo.findByUuidWithOptions(variantType.getId().getValue())).thenReturn(Optional.empty());
        when(variantTypeJpaAssembler.buildFullEntityGraph(eq(variantType), isNull())).thenReturn(new VariantTypeEntity());
        when(variantTypeJpaRepo.save(any(VariantTypeEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value"));

        var exception = assertThrows(com.catalog.infrastructure.exception.CatalogInfraException.class, () -> repository.save(variantType));
        assertEquals("cat.infra.persistence.conflict", exception.getMessageSource().code());
        assertEquals("VariantType", exception.getMessageSource().args().get("resource"));
    }

    @Test
    void findById_loadsEntityWithOptionsAndMapsDomainAggregate() {
        VariantTypeEntity entity = new VariantTypeEntity();
        entity.setUuid("type-color");
        VariantType variantType = createVariantType();

        when(variantTypeJpaRepo.findByUuidWithOptions("type-color")).thenReturn(Optional.of(entity));
        when(variantTypeJpaAssembler.toFullDomainGraph(entity)).thenReturn(variantType);

        Optional<VariantType> result = repository.findById(id("type-color"));

        assertThat(result).contains(variantType);
        verify(variantTypeJpaRepo).findByUuidWithOptions("type-color");
        verify(variantTypeJpaAssembler).toFullDomainGraph(entity);
    }

    private VariantType createVariantType() {
        VariantType variantType = VariantType.create(id("type-color"), "Color");
        variantType.addOption(VariantOption.create(id("opt-red"), "Red", variantType.getId()));
        variantType.addOption(VariantOption.create(id("opt-blue"), "Blue", variantType.getId()));
        return variantType;
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
