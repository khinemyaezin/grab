package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Bin;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.command.UpdateBinCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateBinCommandHandler implements CommandHandler<UpdateBinCommand, LocationResult> {

    private final LocationRepository locationRepository;

    @Override
    @InventoryTransactional
    public LocationResult handle(UpdateBinCommand command) {
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + command.locationId().getValue()));

        Zone zone = location.findZoneById(command.zoneId())
                .orElseThrow(() -> new IllegalArgumentException("Zone not found: " + command.zoneId().getValue()));

        Bin bin = zone.findBinById(command.binId());
        if (bin == null) {
            throw new IllegalArgumentException("Bin not found: " + command.binId().getValue());
        }

        if (command.code() != null && !command.code().isBlank() && !command.code().equals(bin.getCode())) {
            Bin byCode = zone.findBinByCode(command.code());
            if (byCode != null && !byCode.getId().equals(bin.getId())) {
                throw new IllegalArgumentException("Bin already exists for code: " + command.code());
            }
            bin.setCode(command.code());
        }

        if (command.name() != null) {
            bin.setName(command.name());
        }

        if (command.maxCapacity() != null) {
            bin.setMaxCapacity(command.maxCapacity());
        }

        if (command.active() != null) {
            if (command.active()) {
                bin.activate();
            } else {
                bin.deactivate();
            }
        }

        Location saved = locationRepository.save(location);
        return LocationResultMapper.toCommandResult(saved);
    }

    @Override
    public Class<UpdateBinCommand> getCommandType() {
        return UpdateBinCommand.class;
    }
}
