package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.command.ActivateZoneCommand;
import com.grab.store.inventory.internal.command.ZoneResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivateZoneCommandHandler implements CommandHandler<ActivateZoneCommand, ZoneResult> {

    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

    @Override
    @InventoryTransactional
    public ZoneResult handle(ActivateZoneCommand command) {
        log.info("Activating zone with id={}", command.zoneId().getValue());

        Zone zone = zoneRepository.findById(command.zoneId())
                .orElseThrow(() -> {
                    log.warn("Zone not found: zoneId={}", command.zoneId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.ZoneNotFound(command.zoneId().getValue()));
                });

        Location location = locationRepository.findById(zone.getLocationId())
                .orElseThrow(() -> {
                    log.warn("Location not found: locationId={}", zone.getLocationId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.LocationNotFound(zone.getLocationId().getValue()));
                });

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);

        zone.activate();
        Zone saved = zoneRepository.save(zone);

        log.info("Activated zone with id={}, code={}", saved.getId().getValue(), saved.getCode());

        return new ZoneResult(
                saved.getId().getValue(),
                saved.getLocationId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getType().name(),
                saved.isActive()
        );
    }

    @Override
    public Class<ActivateZoneCommand> getCommandType() {
        return ActivateZoneCommand.class;
    }
}
