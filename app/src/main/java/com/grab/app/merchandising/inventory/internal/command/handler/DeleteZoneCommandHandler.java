package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.DeleteZoneCommand;

import com.inventory.application.port.inbound.DeleteZoneUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeleteZoneCommandHandler implements CommandHandler<DeleteZoneCommand, Void> {

    private final DeleteZoneUseCase deleteZoneUseCase;

    public DeleteZoneCommandHandler(DeleteZoneUseCase deleteZoneUseCase) {
        this.deleteZoneUseCase = deleteZoneUseCase;
    }

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
