package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.command.DeleteLocationCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteLocationCommandHandler implements CommandHandler<DeleteLocationCommand, Void> {

    private static final Logger log = Loggers.getLogger(DeleteLocationCommandHandler.class);

    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final ZoneRepository zoneRepository;

    @Override
    @InventoryTransactional
    public Void handle(DeleteLocationCommand command) {
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

    @Override
    public Class<DeleteLocationCommand> getCommandType() {
        return DeleteLocationCommand.class;
    }
}
