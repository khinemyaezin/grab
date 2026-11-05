package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.CreateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.application.port.inbound.CreateZoneUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreateZoneCommandHandler implements CommandHandler<CreateZoneCommand, ZoneResult> {

    private final CreateZoneUseCase createZoneUseCase;

    public CreateZoneCommandHandler(CreateZoneUseCase createZoneUseCase) {
        this.createZoneUseCase = createZoneUseCase;
    }

    @Override
    @InventoryTransactional
    public ZoneResult handle(CreateZoneCommand command) {
        return createZoneUseCase.execute(command);
    }

    @Override
    public Class<CreateZoneCommand> getCommandType() {
        return CreateZoneCommand.class;
    }
}
