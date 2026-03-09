package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.command.RemoveBinCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
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
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + command.locationId().getValue()));

        Zone zone = location.findZoneById(command.zoneId())
                .orElseThrow(() -> new IllegalArgumentException("Zone not found: " + command.zoneId().getValue()));

        boolean removed = zone.removeBin(command.binId());
        if (!removed) {
            throw new IllegalArgumentException("Bin not found: " + command.binId().getValue());
        }

        Location saved = locationRepository.save(location);
        return LocationResultMapper.toCommandResult(saved);
    }

    @Override
    public Class<RemoveBinCommand> getCommandType() {
        return RemoveBinCommand.class;
    }
}
