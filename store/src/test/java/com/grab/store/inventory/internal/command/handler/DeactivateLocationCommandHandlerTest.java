package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.valueobject.Address;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import com.grab.store.inventory.internal.command.DeactivateLocationCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeactivateLocationCommandHandlerTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    private DeactivateLocationCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeactivateLocationCommandHandler(locationRepository, inventoryRepository);
    }

    @Test
    void handle_whenDependentInventoryExists_throws() {
        Location location = location();
        InventoryItem withStock = new InventoryItem(
                id("item-1"),
                "SKU-1",
                id("variant-1"),
                id("loc-1"),
                new InventoryQuantity(10, 0, 0, 0),
                new ReorderConfig(0, 0, 0, null),
                InventoryStatus.ACTIVE,
                LocalDateTime.now()
        );

        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));
        when(inventoryRepository.findByLocation(id("loc-1"))).thenReturn(List.of(withStock));

        assertThatThrownBy(() -> handler.handle(new DeactivateLocationCommand(id("loc-1"), id("actor-1"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot deactivate location with dependent inventory");

        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void handle_deactivatesLocationWhenNoInventoryDependency() {
        Location location = location();

        when(locationRepository.findById(id("loc-1"))).thenReturn(Optional.of(location));
        when(inventoryRepository.findByLocation(id("loc-1"))).thenReturn(List.of());
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocationResult result = handler.handle(new DeactivateLocationCommand(id("loc-1"), id("actor-1")));

        assertThat(result.active()).isFalse();
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
