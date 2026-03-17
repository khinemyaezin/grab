package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.command.RemoveZoneCommand;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.enums.ZoneType;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.valueobject.Address;
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
class RemoveZoneCommandHandlerTest {

    @Mock
    private LocationRepository locationRepository;

    private RemoveZoneCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RemoveZoneCommandHandler(locationRepository);
    }

    @Test
    void handle_removesZone() {
        Location location = locationWithZone();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocationResult result = handler.handle(new RemoveZoneCommand(id("loc-1"), id("zone-1"), id("actor-1")));

        assertThat(result.zones()).isEmpty();
    }

    @Test
    void handle_whenZoneNotFound_throws() {
        Location location = locationWithZone();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));

        assertThatThrownBy(() -> handler.handle(new RemoveZoneCommand(id("loc-1"), id("zone-x"), id("actor-1"))))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Zone not found");

        verify(locationRepository, never()).save(any(Location.class));
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
