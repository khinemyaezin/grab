package com.inventory.application.service;

import com.inventory.application.port.inbound.DeleteLocationUseCase;

import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.port.outbound.ZoneRepository;
import com.inventory.application.model.write.DeleteLocationCommand;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteLocationService implements DeleteLocationUseCase {

    private static final Logger log = Loggers.getLogger(DeleteLocationService.class);

    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final ZoneRepository zoneRepository;

            public Void execute(DeleteLocationCommand command) {
        log.info("Deleting location with id={}", command.locationId().getValue());
        
        
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);

        if (zoneRepository.existsByLocationId(command.locationId())) {
            log.warn("Cannot delete location with dependent zones: locationId={}", command.locationId().getValue());
            throw new InventoryServiceException(
                    new InventoryServiceError.LocationHasDependentZones(command.locationId().getValue()));
        }

        location.delete();
        locationRepository.delete(command.locationId());

        log.info("Deleted location with id={}, code={}", command.locationId().getValue(), location.getCode());

        return null;
    }

        public Class<DeleteLocationCommand> getCommandType() {
        return DeleteLocationCommand.class;
    }
}
