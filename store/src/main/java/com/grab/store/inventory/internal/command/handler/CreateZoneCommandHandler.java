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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateZoneCommandHandler implements CommandHandler<CreateZoneCommand, ZoneResult> {

    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public ZoneResult handle(CreateZoneCommand command) {
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        if (zoneRepository.existsByCodeAndLocationId(command.code(), command.locationId())) {
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
