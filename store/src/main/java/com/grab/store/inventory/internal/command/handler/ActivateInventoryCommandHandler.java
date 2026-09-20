package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.ActivateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.ActivateInventoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivateInventoryCommandHandler implements CommandHandler<ActivateInventoryCommand, InventoryItemResult> {

    private final ActivateInventoryUseCase activateInventoryUseCase;

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
