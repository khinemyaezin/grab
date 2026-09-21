package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.CreateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.application.port.inbound.CreateZoneUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateZoneCommandHandler implements CommandHandler<CreateZoneCommand, ZoneResult> {

    private final CreateZoneUseCase createZoneUseCase;

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
