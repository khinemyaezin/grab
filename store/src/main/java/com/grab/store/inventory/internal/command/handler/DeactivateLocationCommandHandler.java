package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.DeactivateLocationCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateLocationCommandHandler implements CommandHandler<DeactivateLocationCommand, LocationResult> {

    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @InventoryTransactional
    public LocationResult handle(DeactivateLocationCommand command) {
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + command.locationId().getValue()));

        boolean hasInventory = inventoryRepository.findByLocation(command.locationId()).stream()
                .anyMatch(this::hasRemainingStockOrReservations);

        if (hasInventory) {
            throw new IllegalArgumentException(
                    "Cannot deactivate location with dependent inventory: " + command.locationId().getValue());
        }

        location.deactivate();
        Location saved = locationRepository.save(location);

        return LocationResultMapper.toCommandResult(saved);
    }

    @Override
    public Class<DeactivateLocationCommand> getCommandType() {
        return DeactivateLocationCommand.class;
    }

    private boolean hasRemainingStockOrReservations(InventoryItem item) {
        return item.getQuantity().onHand() > 0
                || item.getQuantity().reserved() > 0
                || item.getQuantity().inTransit() > 0
                || item.getQuantity().damaged() > 0;
    }
}
