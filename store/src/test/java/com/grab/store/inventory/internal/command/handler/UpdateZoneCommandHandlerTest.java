package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.enums.ZoneType;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.valueobject.Address;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.command.UpdateZoneCommand;
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
class UpdateZoneCommandHandlerTest {

    @Mock
    private LocationRepository locationRepository;

    private UpdateZoneCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateZoneCommandHandler(locationRepository);
    }

    @Test
    void handle_updatesZoneFields() {
        Location location = locationWithZones();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateZoneCommand command = new UpdateZoneCommand(
                id("loc-1"),
                id("zone-1"),
                "PICK-NEW",
                "Picking Prime",
                ZoneType.STAGING,
                false,
                id("actor-1")
        );

        LocationResult result = handler.handle(command);

        assertThat(result.zones()).hasSize(2);
        LocationResult.Zone updated = result.zones().stream()
                .filter(zone -> "zone-1".equals(zone.id()))
                .findFirst()
                .orElseThrow();
        assertThat(updated.code()).isEqualTo("PICK-NEW");
        assertThat(updated.name()).isEqualTo("Picking Prime");
        assertThat(updated.type()).isEqualTo("STAGING");
        assertThat(updated.active()).isFalse();
    }

    @Test
    void handle_whenCodeConflicts_throws() {
        Location location = locationWithZones();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));

        UpdateZoneCommand command = new UpdateZoneCommand(
                id("loc-1"),
                id("zone-1"),
                "STOR-1",
                null,
                null,
                null,
                id("actor-1")
        );

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Zone already exists for code");

        verify(locationRepository, never()).save(any(Location.class));
    }

    private Location locationWithZones() {
        Location location = new Location(
                id("loc-1"),
                "WH-01",
                "Warehouse 01",
                LocationType.WAREHOUSE,
                new Address("Line 1", null, "Bangkok", null, "10110", "TH")
        );
        location.addZone(new Zone(id("zone-1"), "PICK-1", "Picking", ZoneType.PICKING));
        location.addZone(new Zone(id("zone-2"), "STOR-1", "Storage", ZoneType.STORAGE));
        return location;
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
