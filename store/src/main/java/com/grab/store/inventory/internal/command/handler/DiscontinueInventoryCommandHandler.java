package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.DiscontinueInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.DiscontinueInventoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscontinueInventoryCommandHandler implements CommandHandler<DiscontinueInventoryCommand, InventoryItemResult> {

    private final DiscontinueInventoryUseCase discontinueInventoryUseCase;

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
