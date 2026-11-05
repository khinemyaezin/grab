package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ActivateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.application.port.inbound.ActivateZoneUseCase;
import org.springframework.stereotype.Component;

@Component
public class ActivateZoneCommandHandler implements CommandHandler<ActivateZoneCommand, ZoneResult> {

    private final ActivateZoneUseCase activateZoneUseCase;

    public ActivateZoneCommandHandler(ActivateZoneUseCase activateZoneUseCase) {
        this.activateZoneUseCase = activateZoneUseCase;
    }

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
