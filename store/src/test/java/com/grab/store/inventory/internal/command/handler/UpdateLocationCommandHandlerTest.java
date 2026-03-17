package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.command.UpdateLocationCommand;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateLocationCommandHandlerTest {

    @Mock
    private LocationRepository locationRepository;

    private UpdateLocationCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateLocationCommandHandler(locationRepository);
    }

    @Test
    void handle_updatesCoreFieldsAndAddress() {
        Location location = location("loc-1", "WH-01", "Warehouse 01");
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));
        when(locationRepository.existsByCode("WH-02")).thenReturn(false);
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateLocationCommand command = new UpdateLocationCommand(
                id("loc-1"),
                "WH-02",
                "Warehouse Prime",
                LocationType.STORE,
                "New Line 1",
                null,
                "HCM",
                null,
                "70000",
                "VN",
                true,
                id("actor-1")
        );

        LocationResult result = handler.handle(command);

        assertThat(result.code()).isEqualTo("WH-02");
        assertThat(result.name()).isEqualTo("Warehouse Prime");
        assertThat(result.type()).isEqualTo("STORE");
        assertThat(result.address().country()).isEqualTo("VN");
    }

    @Test
    void handle_whenAddressProvidedWithoutCountry_throws() {
        Location location = location("loc-1", "WH-01", "Warehouse 01");
        location.setAddress(null);
        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));

        UpdateLocationCommand command = new UpdateLocationCommand(
                id("loc-1"),
                null,
                null,
                null,
                "Line 1",
                null,
                "HCM",
                null,
                "70000",
                null,
                true,
                id("actor-1")
        );

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Address country is required");
    }

    private Location location(String id, String code, String name) {
        return new Location(
                id(id),
                code,
                name,
                LocationType.WAREHOUSE,
                new Address("Line 1", null, "Bangkok", null, "10110", "TH")
        );
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
