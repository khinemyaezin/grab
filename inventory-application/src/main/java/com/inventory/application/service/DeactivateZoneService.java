package com.inventory.application.service;

import com.inventory.application.port.inbound.DeactivateZoneUseCase;

import com.inventory.domain.aggregate.Location;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.port.outbound.ZoneRepository;
import com.inventory.application.model.write.DeactivateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeactivateZoneService implements DeactivateZoneUseCase {

    private static final Logger log = Loggers.getLogger(DeactivateZoneService.class);

    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

            public ZoneResult execute(DeactivateZoneCommand command) {
        log.info("Deactivating zone with id={}", command.zoneId().getValue());

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

        zone.deactivate();
        Zone saved = zoneRepository.save(zone);

        log.info("Deactivated zone with id={}, code={}", saved.getId().getValue(), saved.getCode());

        return new ZoneResult(
                saved.getId().getValue(),
                saved.getLocationId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getType().name(),
                saved.isActive()
        );
    }

        public Class<DeactivateZoneCommand> getCommandType() {
        return DeactivateZoneCommand.class;
    }
}
