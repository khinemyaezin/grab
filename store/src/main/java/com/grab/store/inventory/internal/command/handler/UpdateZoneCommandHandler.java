package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.command.UpdateZoneCommand;
import com.grab.store.inventory.internal.command.ZoneResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateZoneCommandHandler implements CommandHandler<UpdateZoneCommand, ZoneResult> {

    private final ZoneRepository zoneRepository;

    @Override
    @InventoryTransactional
    public ZoneResult handle(UpdateZoneCommand command) {
        log.info("Updating zone with id={}", command.zoneId().getValue());
        
        Zone zone = zoneRepository.findById(command.zoneId())
                .orElseThrow(() -> {
                    log.warn("Zone not found: zoneId={}", command.zoneId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.ZoneNotFound(command.zoneId().getValue()));
                });

        if (command.code() != null && !command.code().equals(zone.getCode())) {
            if (zoneRepository.existsByCodeAndLocationId(command.code(), zone.getLocationId())) {
                log.warn("Zone code already exists: code={}, locationId={}", command.code(), zone.getLocationId().getValue());
                throw new InventoryServiceException(
                        new InventoryServiceError.ZoneAlreadyExists(command.code()));
            }
        }

        zone.updateMetadata(command.code(), command.name(), command.type());

        if (command.active() != null) {
            if (command.active()) {
                zone.activate();
            } else {
                zone.deactivate();
            }
        }

        Zone saved = zoneRepository.save(zone);

        log.info("Updated zone with id={}, code={}", saved.getId().getValue(), saved.getCode());

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
    public Class<UpdateZoneCommand> getCommandType() {
        return UpdateZoneCommand.class;
    }
}
