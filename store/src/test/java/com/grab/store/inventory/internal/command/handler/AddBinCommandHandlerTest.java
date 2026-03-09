package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.enums.ZoneType;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.valueobject.Address;
import com.grab.store.inventory.internal.command.AddBinCommand;
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
class AddBinCommandHandlerTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private IdGenerator idGenerator;

    private AddBinCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AddBinCommandHandler(locationRepository, idGenerator);
    }

    @Test
    void handle_addsBinToZone() {
        Location location = locationWithZone();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));
        when(idGenerator.generateId()).thenReturn(id("bin-1"));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddBinCommand command = new AddBinCommand(
                id("loc-1"),
                id("zone-1"),
                "BIN-1",
                "Primary Bin",
                100,
                true,
                id("actor-1")
        );

        LocationResult result = handler.handle(command);

        assertThat(result.zones()).hasSize(1);
        assertThat(result.zones().getFirst().bins()).hasSize(1);
        assertThat(result.zones().getFirst().bins().getFirst().code()).isEqualTo("BIN-1");
    }

    @Test
    void handle_whenZoneNotFound_throws() {
        Location location = locationWithZone();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));

        AddBinCommand command = new AddBinCommand(
                id("loc-1"),
                id("zone-x"),
                "BIN-1",
                "Primary Bin",
                100,
                true,
                id("actor-1")
        );

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Zone not found");
    }

    private Location locationWithZone() {
        Location location = new Location(
                id("loc-1"),
                "WH-01",
                "Warehouse 01",
                LocationType.WAREHOUSE,
                new Address("Line 1", null, "Bangkok", null, "10110", "TH")
        );
        location.addZone(new Zone(id("zone-1"), "PICK-1", "Picking", ZoneType.PICKING));
        return location;
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
