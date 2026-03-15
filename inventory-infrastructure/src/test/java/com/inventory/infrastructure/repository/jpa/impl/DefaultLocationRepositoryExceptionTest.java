package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.valueobject.Address;
import com.inventory.infrastructure.support.InventoryInfrastructureLoggerExtension;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.exception.InventoryInfraException;
import com.inventory.infrastructure.mapper.jpa.LocationJpaAssembler;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.support.InventoryPersistenceExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, InventoryInfrastructureLoggerExtension.class})
class DefaultLocationRepositoryExceptionTest {

    @Mock
    private LocationJpaRepository jpaRepository;

    @Mock
    private LocationJpaAssembler mapper;

    private final InventoryPersistenceExecutor executor = new InventoryPersistenceExecutor();

    @Test
    void save_whenConstraintViolationOccurs_shouldThrowTypedInfrastructureConflict() {
        DefaultLocationRepository repository = new DefaultLocationRepository(jpaRepository, mapper, executor);
        Location location = new Location(
                new CommonId("loc-1"),
                "WH-1",
                "Warehouse",
                LocationType.WAREHOUSE,
                new Address("Line 1", null, "Bangkok", null, "10110", "TH")
        );

        when(jpaRepository.findByUuid("loc-1")).thenReturn(Optional.empty());
        when(mapper.buildFullEntityGraph(any(Location.class), any())).thenReturn(new LocationEntity());
        when(jpaRepository.save(any(LocationEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value"));

        assertThatThrownBy(() -> repository.save(location))
                .isInstanceOf(InventoryInfraException.class)
                .satisfies(exception -> {
                    InventoryInfraException typed = (InventoryInfraException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("inv.infra.persistence.conflict");
                    assertThat(typed.getMessageSource().args()).containsEntry("resource", "Location");
                });
    }
}
