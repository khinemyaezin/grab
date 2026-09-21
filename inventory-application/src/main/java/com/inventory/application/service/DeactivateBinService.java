package com.inventory.application.service;

import com.inventory.application.port.inbound.DeactivateBinUseCase;

import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.port.outbound.BinRepository;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.model.write.DeactivateBinCommand;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.port.outbound.ZoneRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeactivateBinService implements DeactivateBinUseCase {

    private static final Logger log = Loggers.getLogger(DeactivateBinService.class);

    private final BinRepository binRepository;
    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

            public BinResult execute(DeactivateBinCommand command) {
        log.info("Deactivating bin with id={}", command.binId().getValue());
        
        
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


        bin.deactivate();
        Bin saved = binRepository.save(bin);

        log.info("Deactivated bin with id={}, code={}", saved.getId().getValue(), saved.getCode());

        return new BinResult(
                saved.getId().getValue(),
                saved.getZoneId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getMaxCapacity(),
                saved.isActive()
        );
    }

        public Class<DeactivateBinCommand> getCommandType() {
        return DeactivateBinCommand.class;
    }
}
