package com.inventory.application.service;

import com.inventory.application.port.inbound.ActivateLocationUseCase;

import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.application.model.write.ActivateLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ActivateLocationService implements ActivateLocationUseCase {

    private static final Logger log = Loggers.getLogger(ActivateLocationService.class);

    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

            public LocationResult execute(ActivateLocationCommand command) {
        log.info("Activating location with id={}", command.locationId().getValue());
        
        
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);

        location.activate();
        Location saved = locationRepository.save(location);

        log.info("Activated location with id={}, code={}", saved.getId().getValue(), saved.getCode());

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

        public Class<ActivateLocationCommand> getCommandType() {
        return ActivateLocationCommand.class;
    }
}
