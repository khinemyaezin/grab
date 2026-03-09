package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Bin;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.enums.ZoneType;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.valueobject.Address;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.command.RemoveBinCommand;
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
class RemoveBinCommandHandlerTest {

    @Mock
    private LocationRepository locationRepository;

    private RemoveBinCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RemoveBinCommandHandler(locationRepository);
    }

    @Test
    void handle_removesBin() {
        Location location = locationWithBin();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocationResult result = handler.handle(new RemoveBinCommand(id("loc-1"), id("zone-1"), id("bin-1"), id("actor-1")));

        assertThat(result.zones()).hasSize(1);
        assertThat(result.zones().getFirst().bins()).isEmpty();
    }

    @Test
    void handle_whenBinNotFound_throws() {
        Location location = locationWithBin();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));

        assertThatThrownBy(() -> handler.handle(new RemoveBinCommand(id("loc-1"), id("zone-1"), id("bin-x"), id("actor-1"))))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Bin not found");

        verify(locationRepository, never()).save(any(Location.class));
    }

    private Location locationWithBin() {
        Location location = new Location(
                id("loc-1"),
                "WH-01",
                "Warehouse 01",
                LocationType.WAREHOUSE,
                new Address("Line 1", null, "Bangkok", null, "10110", "TH")
        );
        Zone zone = new Zone(id("zone-1"), "PICK-1", "Picking", ZoneType.PICKING);
        zone.addBin(new Bin(id("bin-1"), "BIN-1", "Bin 1", 100, true));
        location.addZone(zone);
        return location;
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
