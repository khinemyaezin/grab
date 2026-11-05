package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.SuspendInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.SuspendInventoryUseCase;
import org.springframework.stereotype.Component;

@Component
public class SuspendInventoryCommandHandler implements CommandHandler<SuspendInventoryCommand, InventoryItemResult> {

    private final SuspendInventoryUseCase suspendInventoryUseCase;

    public SuspendInventoryCommandHandler(SuspendInventoryUseCase suspendInventoryUseCase) {
        this.suspendInventoryUseCase = suspendInventoryUseCase;
    }

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(SuspendInventoryCommand command) {
        return suspendInventoryUseCase.execute(command);
    }

    @Override
    public Class<SuspendInventoryCommand> getCommandType() {
        return SuspendInventoryCommand.class;
    }
}
