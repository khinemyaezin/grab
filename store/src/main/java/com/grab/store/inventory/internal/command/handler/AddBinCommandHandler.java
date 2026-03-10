package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Bin;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.AddBinCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddBinCommandHandler implements CommandHandler<AddBinCommand, LocationResult> {

    private final LocationRepository locationRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public LocationResult handle(AddBinCommand command) {
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        Zone zone = location.findZoneById(command.zoneId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.ZoneNotFound(command.zoneId().getValue())));

        if (zone.findBinByCode(command.code()) != null) {
            throw new InventoryServiceException(new InventoryServiceError.BinAlreadyExists(command.code()));
        }

        Bin bin = new Bin(
                idGenerator.generateId(),
                command.code(),
                command.name(),
                command.maxCapacity(),
                !Boolean.FALSE.equals(command.active())
        );

        boolean added = zone.addBin(bin);
        if (!added) {
            throw new InventoryServiceException(new InventoryServiceError.UnableToAddBin(command.code()));
        }

        Location saved = locationRepository.save(location);
        return LocationResultMapper.toCommandResult(saved);
    }

    @Override
    public Class<AddBinCommand> getCommandType() {
        return AddBinCommand.class;
    }
}
