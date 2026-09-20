package com.inventory.application.service;

import com.inventory.application.port.inbound.UpdateBinUseCase;

import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.port.outbound.BinRepository;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.model.write.UpdateBinCommand;
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
public class UpdateBinService implements UpdateBinUseCase {

    private static final Logger log = Loggers.getLogger(UpdateBinService.class);

    private final BinRepository binRepository;
    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

            public BinResult execute(UpdateBinCommand command) {
        log.info("Updating bin with id={}", command.binId().getValue());
        
        
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


        if (command.code() != null && !command.code().equals(bin.getCode())) {
            if (binRepository.existsByCodeAndZoneId(command.code(), bin.getZoneId())) {
                log.warn("Bin code already exists: code={}, zoneId={}", command.code(), bin.getZoneId().getValue());
                throw new InventoryServiceException(
                        new InventoryServiceError.BinAlreadyExists(command.code()));
            }
        }

        bin.update(command.code(), command.name(), command.maxCapacity());

        if (command.active() != null) {
            if (command.active()) {
                bin.activate();
            } else {
                bin.deactivate();
            }
        }

        Bin saved = binRepository.save(bin);

        log.info("Updated bin with id={}, code={}", saved.getId().getValue(), saved.getCode());

        return new BinResult(
                saved.getId().getValue(),
                saved.getZoneId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getMaxCapacity(),
                saved.isActive()
        );
    }

        public Class<UpdateBinCommand> getCommandType() {
        return UpdateBinCommand.class;
    }
}
