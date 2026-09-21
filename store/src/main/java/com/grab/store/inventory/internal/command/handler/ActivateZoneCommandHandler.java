package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.ActivateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.application.port.inbound.ActivateZoneUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivateZoneCommandHandler implements CommandHandler<ActivateZoneCommand, ZoneResult> {

    private final ActivateZoneUseCase activateZoneUseCase;

    @Override
    @InventoryTransactional
    public ZoneResult handle(ActivateZoneCommand command) {
        return activateZoneUseCase.execute(command);
    }

    @Override
    public Class<ActivateZoneCommand> getCommandType() {
        return ActivateZoneCommand.class;
    }
}
