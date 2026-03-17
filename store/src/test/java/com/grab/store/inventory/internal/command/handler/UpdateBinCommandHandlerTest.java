package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.command.UpdateBinCommand;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Bin;
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
class UpdateBinCommandHandlerTest {

    @Mock
    private LocationRepository locationRepository;

    private UpdateBinCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateBinCommandHandler(locationRepository);
    }

    @Test
    void handle_updatesBinFields() {
        Location location = locationWithBins();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateBinCommand command = new UpdateBinCommand(
                id("loc-1"),
                id("zone-1"),
                id("bin-1"),
                "BIN-NEW",
                "Bin Prime",
                200,
                false,
                id("actor-1")
        );

        LocationResult result = handler.handle(command);

        LocationResult.Bin updated = result.zones().getFirst().bins().stream()
                .filter(bin -> "bin-1".equals(bin.id()))
                .findFirst()
                .orElseThrow();
        assertThat(updated.code()).isEqualTo("BIN-NEW");
        assertThat(updated.name()).isEqualTo("Bin Prime");
        assertThat(updated.maxCapacity()).isEqualTo(200);
        assertThat(updated.active()).isFalse();
    }

    @Test
    void handle_whenCodeConflicts_throws() {
        Location location = locationWithBins();
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));

        UpdateBinCommand command = new UpdateBinCommand(
                id("loc-1"),
                id("zone-1"),
                id("bin-1"),
                "BIN-2",
                null,
                null,
                null,
                id("actor-1")
        );

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Bin already exists for code");

        verify(locationRepository, never()).save(any(Location.class));
    }

    private Location locationWithBins() {
        Location location = new Location(
                id("loc-1"),
                "WH-01",
                "Warehouse 01",
                LocationType.WAREHOUSE,
                new Address("Line 1", null, "Bangkok", null, "10110", "TH")
        );
        Zone zone = new Zone(id("zone-1"), "PICK-1", "Picking", ZoneType.PICKING);
        zone.addBin(new Bin(id("bin-1"), "BIN-1", "Bin 1", 100, true));
        zone.addBin(new Bin(id("bin-2"), "BIN-2", "Bin 2", 120, true));
        location.addZone(zone);
        return location;
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
