package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.command.DeleteLocationCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteLocationCommandHandler implements CommandHandler<DeleteLocationCommand, Void> {

    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;

    @Override
    @InventoryTransactional
    public Void handle(DeleteLocationCommand command) {
        log.info("Deleting location with id={}", command.locationId().getValue());
        
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> {
                    log.warn("Location not found: locationId={}", command.locationId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.LocationNotFound(command.locationId().getValue()));
                });

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
