package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.valueobject.Address;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateInventoryCommandHandlerTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private IdGenerator idGenerator;

    private CreateInventoryCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateInventoryCommandHandler(
                inventoryRepository,
                stockMovementRepository,
                locationRepository,
                idGenerator
        );
    }

    @Test
    void handle_shouldFailWhenLocationNotFound() {
        CreateInventoryCommand command = createCommand();
        when(locationRepository.findById(command.locationId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Location not found");

        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void handle_shouldFailWhenLocationInactive() {
        CreateInventoryCommand command = createCommand();
        Location inactiveLocation = new Location(
                command.locationId(),
                "WH-01",
                "Warehouse 01",
                LocationType.WAREHOUSE,
                new Address("Street", null, "City", "State", "10000", "VN")
        );
        inactiveLocation.setActive(false);

        when(locationRepository.findById(command.locationId())).thenReturn(Optional.of(inactiveLocation));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Location is not active");

        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    private CreateInventoryCommand createCommand() {
        Id productVariantId = new CommonId("variant-1");
        Id locationId = new CommonId("location-1");
        Id createdBy = new CommonId("actor-1");

        return new CreateInventoryCommand(
                "SKU-1",
                productVariantId,
                locationId,
                10,
                5,
                10,
                20,
                100,
                createdBy
        );
    }
}
