package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.command.UpdateZoneCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateZoneCommandHandler implements CommandHandler<UpdateZoneCommand, LocationResult> {

    private final LocationRepository locationRepository;

    @Override
    @InventoryTransactional
    public LocationResult handle(UpdateZoneCommand command) {
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        Zone zone = location.findZoneById(command.zoneId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.ZoneNotFound(command.zoneId().getValue())));

        if (command.code() != null && !command.code().isBlank() && !command.code().equals(zone.getCode())) {
            location.findZoneByCode(command.code())
                    .filter(existing -> !existing.getId().equals(zone.getId()))
                    .ifPresent(existing -> {
                        throw new InventoryServiceException(new InventoryServiceError.ZoneAlreadyExists(command.code()));
                    });
            zone.setCode(command.code());
        }

        if (command.name() != null && !command.name().isBlank()) {
            zone.setName(command.name());
        }

        if (command.type() != null) {
            zone.setType(command.type());
        }

        if (command.active() != null) {
            if (command.active()) {
                zone.activate();
            } else {
                zone.deactivate();
            }
        }

        Location saved = locationRepository.save(location);
        return LocationResultMapper.toCommandResult(saved);
    }

    @Override
    public Class<UpdateZoneCommand> getCommandType() {
        return UpdateZoneCommand.class;
    }
}
