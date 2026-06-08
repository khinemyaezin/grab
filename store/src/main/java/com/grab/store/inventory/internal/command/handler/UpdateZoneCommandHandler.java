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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateZoneCommandHandler implements CommandHandler<UpdateZoneCommand, ZoneResult> {

    private final ZoneRepository zoneRepository;

    @Override
    @InventoryTransactional
    public ZoneResult handle(UpdateZoneCommand command) {
        Zone zone = zoneRepository.findById(command.zoneId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.ZoneNotFound(command.zoneId().getValue())));

        if (command.code() != null && !command.code().equals(zone.getCode())) {
            if (zoneRepository.existsByCodeAndLocationId(command.code(), zone.getLocationId())) {
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
