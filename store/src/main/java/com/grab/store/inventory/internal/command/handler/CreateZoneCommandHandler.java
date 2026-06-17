package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.command.CreateZoneCommand;
import com.grab.store.inventory.internal.command.ZoneResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateZoneCommandHandler implements CommandHandler<CreateZoneCommand, ZoneResult> {

    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public ZoneResult handle(CreateZoneCommand command) {
        log.info("Creating zone with code={} for locationId={}", command.code(), command.locationId().getValue());
        
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> {
                    log.warn("Location not found: locationId={}", command.locationId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.LocationNotFound(command.locationId().getValue()));
                });

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

    @Override
    public Class<CreateZoneCommand> getCommandType() {
        return CreateZoneCommand.class;
    }
}
