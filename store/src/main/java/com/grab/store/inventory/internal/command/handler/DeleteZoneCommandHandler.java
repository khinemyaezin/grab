package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.repository.BinRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.command.DeleteZoneCommand;
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
public class DeleteZoneCommandHandler implements CommandHandler<DeleteZoneCommand, Void> {

    private static final Logger log = Loggers.getLogger(DeleteZoneCommandHandler.class);

    private final ZoneRepository zoneRepository;
    private final BinRepository binRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

    @Override
    @InventoryTransactional
    public Void handle(DeleteZoneCommand command) {
        log.info("Deleting zone with id={}", command.zoneId().getValue());

        Zone zone = zoneRepository.findById(command.zoneId())
                .orElseThrow(() -> {
                    log.warn("Zone not found: zoneId={}", command.zoneId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.ZoneNotFound(command.zoneId().getValue()));
                });

        Location location = locationRepository.findById(zone.getLocationId())
                .orElseThrow(() -> {
                    log.warn("Location not found: locationId={}", zone.getLocationId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.LocationNotFound(zone.getLocationId().getValue()));
                });

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);

        if (binRepository.existsByZoneId(command.zoneId())) {
            log.warn("Cannot delete zone with dependent bins: zoneId={}", command.zoneId().getValue());
            throw new InventoryServiceException(
                    new InventoryServiceError.ZoneHasDependentBins(command.zoneId().getValue()));
        }

        zone.delete();
        zoneRepository.delete(command.zoneId());

        log.info("Deleted zone with id={}, code={}", command.zoneId().getValue(), zone.getCode());

        return null;
    }

    @Override
    public Class<DeleteZoneCommand> getCommandType() {
        return DeleteZoneCommand.class;
    }
}
