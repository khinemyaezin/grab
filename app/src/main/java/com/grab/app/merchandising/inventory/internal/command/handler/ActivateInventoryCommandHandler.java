package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ActivateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.ActivateInventoryUseCase;
import org.springframework.stereotype.Component;

@Component
public class ActivateInventoryCommandHandler implements CommandHandler<ActivateInventoryCommand, InventoryItemResult> {

    private final ActivateInventoryUseCase activateInventoryUseCase;

    public ActivateInventoryCommandHandler(ActivateInventoryUseCase activateInventoryUseCase) {
        this.activateInventoryUseCase = activateInventoryUseCase;
    }

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(ActivateInventoryCommand command) {
        return activateInventoryUseCase.execute(command);
    }

    @Override
    public Class<ActivateInventoryCommand> getCommandType() {
        return ActivateInventoryCommand.class;
    }
}
