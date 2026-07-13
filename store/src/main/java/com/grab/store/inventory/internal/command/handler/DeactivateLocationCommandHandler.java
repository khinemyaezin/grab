package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.DeactivateLocationCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeactivateLocationCommandHandler implements CommandHandler<DeactivateLocationCommand, LocationResult> {

    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryRepository inventoryRepository;

    @Override
    @InventoryTransactional
    public LocationResult handle(DeactivateLocationCommand command) {
        log.info("Deactivating location with id={}", command.locationId().getValue());
        
        
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);

        boolean hasInventory = inventoryRepository.findByLocation(command.locationId()).stream()
                .anyMatch(this::hasRemainingStockOrReservations);

        if (hasInventory) {
            log.warn("Cannot deactivate location with dependent inventory: locationId={}", command.locationId().getValue());
            throw new InventoryServiceException(new InventoryServiceError.LocationHasDependentInventory(command.locationId().getValue()));
        }

        location.deactivate();
        Location saved = locationRepository.save(location);

        log.info("Deactivated location with id={}, code={}", saved.getId().getValue(), saved.getCode());

        return new LocationResult(
                saved.getId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getType().name(),
                saved.isActive(),
                new LocationResult.Address(
                        saved.getAddress().line1(),
                        saved.getAddress().line2(),
                        saved.getAddress().city(),
                        saved.getAddress().state(),
                        saved.getAddress().postalCode(),
                        saved.getAddress().country()
                )
        );
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
