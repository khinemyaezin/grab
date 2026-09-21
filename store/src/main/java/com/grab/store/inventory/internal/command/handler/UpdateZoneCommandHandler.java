package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.UpdateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
import com.inventory.application.port.inbound.UpdateZoneUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateZoneCommandHandler implements CommandHandler<UpdateZoneCommand, ZoneResult> {

    private final UpdateZoneUseCase updateZoneUseCase;

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
