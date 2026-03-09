package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.enums.ZoneType;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.valueobject.Address;
import com.grab.store.inventory.internal.command.AddZoneCommand;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.command.LocationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddZoneCommandHandlerTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private IdGenerator idGenerator;

    private AddZoneCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AddZoneCommandHandler(locationRepository, idGenerator);
    }

    @Test
    void handle_addsZone() {
        Location location = location();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));
        when(idGenerator.generateId()).thenReturn(id("zone-1"));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddZoneCommand command = new AddZoneCommand(
                id("loc-1"),
                "PICK-1",
                "Picking Zone",
                ZoneType.PICKING,
                true,
                id("actor-1")
        );

        LocationResult result = handler.handle(command);

        assertThat(result.zones()).hasSize(1);
        assertThat(result.zones().getFirst().code()).isEqualTo("PICK-1");
    }

    @Test
    void handle_whenDuplicateCode_throws() {
        Location location = location();
        location.addZone(new com.inventory.domain.entity.Zone(id("zone-1"), "PICK-1", "Z1", ZoneType.PICKING));

        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));

        AddZoneCommand command = new AddZoneCommand(
                id("loc-1"),
                "PICK-1",
                "Picking Zone",
                ZoneType.PICKING,
                true,
                id("actor-1")
        );

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Zone already exists for code");

        verify(locationRepository, never()).save(any(Location.class));
    }

    private Location location() {
        return new Location(
                id("loc-1"),
                "WH-01",
                "Warehouse 01",
                LocationType.WAREHOUSE,
                new Address("Line 1", null, "Bangkok", null, "10110", "TH")
        );
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
