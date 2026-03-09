package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.ActivateLocationCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivateLocationCommandHandler implements CommandHandler<ActivateLocationCommand, LocationResult> {

    private final LocationRepository locationRepository;

    @Override
    @InventoryTransactional
    public LocationResult handle(ActivateLocationCommand command) {
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        location.activate();
        Location saved = locationRepository.save(location);

        return LocationResultMapper.toCommandResult(saved);
    }

    @Override
    public Class<ActivateLocationCommand> getCommandType() {
        return ActivateLocationCommand.class;
    }
}
