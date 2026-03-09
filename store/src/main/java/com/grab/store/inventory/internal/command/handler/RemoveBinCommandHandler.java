package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.command.RemoveBinCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveBinCommandHandler implements CommandHandler<RemoveBinCommand, LocationResult> {

    private final LocationRepository locationRepository;

    @Override
    @InventoryTransactional
    public LocationResult handle(RemoveBinCommand command) {
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        Zone zone = location.findZoneById(command.zoneId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.ZoneNotFound(command.zoneId().getValue())));

        boolean removed = zone.removeBin(command.binId());
        if (!removed) {
            throw new InventoryServiceException(new InventoryServiceError.BinNotFound(command.binId().getValue()));
        }

        Location saved = locationRepository.save(location);
        return LocationResultMapper.toCommandResult(saved);
    }

    @Override
    public Class<RemoveBinCommand> getCommandType() {
        return RemoveBinCommand.class;
    }
}
