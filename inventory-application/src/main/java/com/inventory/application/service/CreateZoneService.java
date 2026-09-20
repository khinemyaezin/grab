package com.inventory.application.service;

import com.inventory.application.port.inbound.CreateZoneUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.port.outbound.ZoneRepository;
import com.inventory.application.model.write.CreateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateZoneService implements CreateZoneUseCase {

    private static final Logger log = Loggers.getLogger(CreateZoneService.class);

    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final IdGenerator idGenerator;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

            public ZoneResult execute(CreateZoneCommand command) {
        log.info("Creating zone with code={} for locationId={}", command.code(), command.locationId().getValue());

        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> {
                    log.warn("Location not found: locationId={}", command.locationId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.LocationNotFound(command.locationId().getValue()));
                });

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);

        if (zoneRepository.existsByCodeAndLocationId(command.code(), command.locationId())) {
            log.warn("Zone already exists with code={} in locationId={}", command.code(), command.locationId().getValue());
            throw new InventoryServiceException(
                    new InventoryServiceError.ZoneAlreadyExists(command.code()));
        }

        Zone zone = Zone.create(
                idGenerator.generateId(),
                command.locationId(),
                command.code(),
                command.name(),
                command.type()
        );

        Zone saved = zoneRepository.save(zone);

        log.info("Created zone with id={}, code={}, locationId={}", saved.getId().getValue(), saved.getCode(), saved.getLocationId().getValue());

        return new ZoneResult(
                saved.getId().getValue(),
                saved.getLocationId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getType().name(),
                saved.isActive()
        );
    }

        public Class<CreateZoneCommand> getCommandType() {
        return CreateZoneCommand.class;
    }
}
