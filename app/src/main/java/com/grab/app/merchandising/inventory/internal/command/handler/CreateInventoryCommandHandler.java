package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.CreateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.CreateInventoryUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreateInventoryCommandHandler implements CommandHandler<CreateInventoryCommand, InventoryItemResult> {

    private final CreateInventoryUseCase createInventoryUseCase;

    public CreateInventoryCommandHandler(CreateInventoryUseCase createInventoryUseCase) {
        this.createInventoryUseCase = createInventoryUseCase;
    }

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(CreateInventoryCommand command) {
        return createInventoryUseCase.execute(command);
    }

    @Override
    public Class<CreateInventoryCommand> getCommandType() {
        return CreateInventoryCommand.class;
    }
}
