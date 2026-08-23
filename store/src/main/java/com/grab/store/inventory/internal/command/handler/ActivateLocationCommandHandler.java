package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.ActivateLocationCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivateLocationCommandHandler implements CommandHandler<ActivateLocationCommand, LocationResult> {

    private static final Logger log = Loggers.getLogger(ActivateLocationCommandHandler.class);

    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

    @Override
    @InventoryTransactional
    public LocationResult handle(ActivateLocationCommand command) {
        log.info("Activating location with id={}", command.locationId().getValue());
        
        
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);

        location.activate();
        Location saved = locationRepository.save(location);

        log.info("Activated location with id={}, code={}", saved.getId().getValue(), saved.getCode());

        return new LocationResult(
                saved.getId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getType().name(),
                saved.isActive(),
                new LocationResult.Address(
                        saved.getAddress().line1(),
                        saved.getAddress().line2(),
                        saved.getAddress().city(),
                        saved.getAddress().state(),
                        saved.getAddress().postalCode(),
                        saved.getAddress().country()
                )
        );
    }

    @Override
    public Class<ActivateLocationCommand> getCommandType() {
        return ActivateLocationCommand.class;
    }
}
