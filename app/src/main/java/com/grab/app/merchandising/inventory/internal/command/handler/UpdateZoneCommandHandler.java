package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.UpdateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.application.port.inbound.UpdateZoneUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdateZoneCommandHandler implements CommandHandler<UpdateZoneCommand, ZoneResult> {

    private final UpdateZoneUseCase updateZoneUseCase;

    public UpdateZoneCommandHandler(UpdateZoneUseCase updateZoneUseCase) {
        this.updateZoneUseCase = updateZoneUseCase;
    }

    @Override
    @InventoryTransactional
    public ZoneResult handle(UpdateZoneCommand command) {
        return updateZoneUseCase.execute(command);
    }

    @Override
    public Class<UpdateZoneCommand> getCommandType() {
        return UpdateZoneCommand.class;
    }
}
