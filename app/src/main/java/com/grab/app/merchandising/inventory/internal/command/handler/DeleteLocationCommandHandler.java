package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.DeleteLocationCommand;

import com.inventory.application.port.inbound.DeleteLocationUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeleteLocationCommandHandler implements CommandHandler<DeleteLocationCommand, Void> {

    private final DeleteLocationUseCase deleteLocationUseCase;

    public DeleteLocationCommandHandler(DeleteLocationUseCase deleteLocationUseCase) {
        this.deleteLocationUseCase = deleteLocationUseCase;
    }

    @Override
    @InventoryTransactional
    public Void handle(DeleteLocationCommand command) {
        return deleteLocationUseCase.execute(command);
    }

    @Override
    public Class<DeleteLocationCommand> getCommandType() {
        return DeleteLocationCommand.class;
    }
}
