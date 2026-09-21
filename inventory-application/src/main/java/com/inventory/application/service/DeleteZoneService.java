package com.inventory.application.service;

import com.inventory.application.port.inbound.DeleteZoneUseCase;

import com.inventory.domain.aggregate.Location;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.port.outbound.BinRepository;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.port.outbound.ZoneRepository;
import com.inventory.application.model.write.DeleteZoneCommand;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteZoneService implements DeleteZoneUseCase {

    private static final Logger log = Loggers.getLogger(DeleteZoneService.class);

    private final ZoneRepository zoneRepository;
    private final BinRepository binRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

            public Void execute(DeleteZoneCommand command) {
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

        public Class<DeleteZoneCommand> getCommandType() {
        return DeleteZoneCommand.class;
    }
}
