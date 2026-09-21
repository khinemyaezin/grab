package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.DeleteZoneCommand;
import com.inventory.application.port.inbound.DeleteZoneUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteZoneCommandHandler implements CommandHandler<DeleteZoneCommand, Void> {

    private final DeleteZoneUseCase deleteZoneUseCase;

    @Override
    @InventoryTransactional
    public Void handle(DeleteZoneCommand command) {
        return deleteZoneUseCase.execute(command);
    }

    @Override
    public Class<DeleteZoneCommand> getCommandType() {
        return DeleteZoneCommand.class;
    }
}
