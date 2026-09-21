package com.inventory.application.service;

import com.inventory.application.port.inbound.CreateBinUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.port.outbound.BinRepository;
import com.inventory.domain.port.outbound.ZoneRepository;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.model.write.CreateBinCommand;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.LocationRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateBinService implements CreateBinUseCase {

    private static final Logger log = Loggers.getLogger(CreateBinService.class);

    private final ZoneRepository zoneRepository;
    private final BinRepository binRepository;
    private final IdGenerator idGenerator;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

            public BinResult execute(CreateBinCommand command) {
        log.info("Creating bin with code={} for zoneId={}", command.code(), command.zoneId().getValue());

        Zone zone = zoneRepository.findById(command.zoneId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.ZoneNotFound(command.zoneId().getValue())));

        Location location = locationRepository.findById(zone.getLocationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(zone.getLocationId().getValue())));

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);
        


        if (binRepository.existsByCodeAndZoneId(command.code(), command.zoneId())) {
            log.warn("Bin already exists with code={} in zoneId={}", command.code(), command.zoneId().getValue());
            throw new InventoryServiceException(
                    new InventoryServiceError.BinAlreadyExists(command.code()));
        }

        Bin bin = Bin.create(
                idGenerator.generateId(),
                command.zoneId(),
                command.code(),
                command.name(),
                command.maxCapacity()
        );

        Bin saved = binRepository.save(bin);

        log.info("Created bin with id={}, code={}, zoneId={}", saved.getId().getValue(), saved.getCode(), saved.getZoneId().getValue());

        return new BinResult(
                saved.getId().getValue(),
                saved.getZoneId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getMaxCapacity(),
                saved.isActive()
        );
    }

        public Class<CreateBinCommand> getCommandType() {
        return CreateBinCommand.class;
    }
}
