package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.command.DeactivateZoneCommand;
import com.grab.store.inventory.internal.command.ZoneResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateZoneCommandHandler implements CommandHandler<DeactivateZoneCommand, ZoneResult> {

    private final ZoneRepository zoneRepository;

    @Override
    @InventoryTransactional
    public ZoneResult handle(DeactivateZoneCommand command) {
        Zone zone = zoneRepository.findById(command.zoneId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.ZoneNotFound(command.zoneId().getValue())));

        zone.deactivate();
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
    public Class<DeactivateZoneCommand> getCommandType() {
        return DeactivateZoneCommand.class;
    }
}
