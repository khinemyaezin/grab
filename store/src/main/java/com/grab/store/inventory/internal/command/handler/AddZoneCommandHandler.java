package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.AddZoneCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddZoneCommandHandler implements CommandHandler<AddZoneCommand, LocationResult> {

    private final LocationRepository locationRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public LocationResult handle(AddZoneCommand command) {
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        if (location.findZoneByCode(command.code()).isPresent()) {
            throw new InventoryServiceException(new InventoryServiceError.ZoneAlreadyExists(command.code()));
        }

        Zone zone = new Zone(idGenerator.generateId(), command.code(), command.name(), command.type());
        if (Boolean.FALSE.equals(command.active())) {
            zone.deactivate();
        }

        boolean added = location.addZone(zone);
        if (!added) {
            throw new InventoryServiceException(new InventoryServiceError.UnableToAddZone(command.code()));
        }

        Location saved = locationRepository.save(location);
        return LocationResultMapper.toCommandResult(saved);
    }

    @Override
    public Class<AddZoneCommand> getCommandType() {
        return AddZoneCommand.class;
    }
}
