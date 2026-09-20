package com.inventory.application.service;

import com.inventory.application.port.inbound.DeactivateLocationUseCase;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.application.model.write.DeactivateLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeactivateLocationService implements DeactivateLocationUseCase {

    private static final Logger log = Loggers.getLogger(DeactivateLocationService.class);

    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryRepository inventoryRepository;

            public LocationResult execute(DeactivateLocationCommand command) {
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
