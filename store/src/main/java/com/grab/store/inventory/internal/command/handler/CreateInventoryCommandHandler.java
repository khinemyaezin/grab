package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.CreateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.CreateInventoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateInventoryCommandHandler implements CommandHandler<CreateInventoryCommand, InventoryItemResult> {

    private final CreateInventoryUseCase createInventoryUseCase;

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
