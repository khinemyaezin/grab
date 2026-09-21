package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.DeleteLocationCommand;
import com.inventory.application.port.inbound.DeleteLocationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteLocationCommandHandler implements CommandHandler<DeleteLocationCommand, Void> {

    private final DeleteLocationUseCase deleteLocationUseCase;

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
