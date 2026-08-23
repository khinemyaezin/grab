package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.repository.BinRepository;
import com.grab.store.inventory.internal.command.ActivateBinCommand;
import com.grab.store.inventory.internal.command.BinResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivateBinCommandHandler implements CommandHandler<ActivateBinCommand, BinResult> {

    private static final Logger log = Loggers.getLogger(ActivateBinCommandHandler.class);

    private final BinRepository binRepository;
    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

    @Override
    @InventoryTransactional
    public BinResult handle(ActivateBinCommand command) {
        log.info("Activating bin with id={}", command.binId().getValue());
        
        
        Bin bin = binRepository.findById(command.binId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.BinNotFound(command.binId().getValue())));

        Zone zone = zoneRepository.findById(bin.getZoneId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.ZoneNotFound(bin.getZoneId().getValue())));

        Location location = locationRepository.findById(zone.getLocationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(zone.getLocationId().getValue())));

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);


        bin.activate();
        Bin saved = binRepository.save(bin);

        log.info("Activated bin with id={}, code={}", saved.getId().getValue(), saved.getCode());

        return new BinResult(
                saved.getId().getValue(),
                saved.getZoneId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getMaxCapacity(),
                saved.isActive()
        );
    }

    @Override
    public Class<ActivateBinCommand> getCommandType() {
        return ActivateBinCommand.class;
    }
}
