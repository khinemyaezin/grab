package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.CreateLocationCommand;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.command.LocationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateLocationCommandHandlerTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private IdGenerator idGenerator;

    private CreateLocationCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateLocationCommandHandler(locationRepository, idGenerator);
    }

    @Test
    void handle_createsAndSavesLocation() {
        CreateLocationCommand command = new CreateLocationCommand(
                "WH-01", "Warehouse 01", LocationType.WAREHOUSE,
                "Line 1", null, "Bangkok", null, "10110", "TH",
                id("actor-1")
        );

        when(locationRepository.existsByCode("WH-01")).thenReturn(false);
        when(idGenerator.generateId()).thenReturn(id("loc-1"));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocationResult result = handler.handle(command);

        assertThat(result.id()).isEqualTo("loc-1");
        assertThat(result.code()).isEqualTo("WH-01");
        assertThat(result.name()).isEqualTo("Warehouse 01");
        assertThat(result.type()).isEqualTo("WAREHOUSE");
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void handle_whenCodeExists_throws() {
        CreateLocationCommand command = new CreateLocationCommand(
                "WH-01", "Warehouse 01", LocationType.WAREHOUSE,
                "Line 1", null, "Bangkok", null, "10110", "TH",
                id("actor-1")
        );

        when(locationRepository.existsByCode("WH-01")).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Location already exists for code");

        verify(locationRepository, never()).save(any(Location.class));
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
