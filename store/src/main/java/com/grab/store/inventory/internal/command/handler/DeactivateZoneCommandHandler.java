package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.DeactivateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.application.port.inbound.DeactivateZoneUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateZoneCommandHandler implements CommandHandler<DeactivateZoneCommand, ZoneResult> {

    private final DeactivateZoneUseCase deactivateZoneUseCase;

    @Override
    @InventoryTransactional
    public ZoneResult handle(DeactivateZoneCommand command) {
        return deactivateZoneUseCase.execute(command);
    }

    @Override
    public Class<DeactivateZoneCommand> getCommandType() {
        return DeactivateZoneCommand.class;
    }
}
