package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.DiscontinueInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.DiscontinueInventoryUseCase;
import org.springframework.stereotype.Component;

@Component
public class DiscontinueInventoryCommandHandler implements CommandHandler<DiscontinueInventoryCommand, InventoryItemResult> {

    private final DiscontinueInventoryUseCase discontinueInventoryUseCase;

    public DiscontinueInventoryCommandHandler(DiscontinueInventoryUseCase discontinueInventoryUseCase) {
        this.discontinueInventoryUseCase = discontinueInventoryUseCase;
    }

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(DiscontinueInventoryCommand command) {
        return discontinueInventoryUseCase.execute(command);
    }

    @Override
    public Class<DiscontinueInventoryCommand> getCommandType() {
        return DiscontinueInventoryCommand.class;
    }
}
