package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.DeactivateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.application.port.inbound.DeactivateZoneUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeactivateZoneCommandHandler implements CommandHandler<DeactivateZoneCommand, ZoneResult> {

    private final DeactivateZoneUseCase deactivateZoneUseCase;

    public DeactivateZoneCommandHandler(DeactivateZoneUseCase deactivateZoneUseCase) {
        this.deactivateZoneUseCase = deactivateZoneUseCase;
    }

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
