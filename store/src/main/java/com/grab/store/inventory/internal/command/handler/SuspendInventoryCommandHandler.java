package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.SuspendInventoryCommand;
import com.inventory.application.port.inbound.SuspendInventoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuspendInventoryCommandHandler implements CommandHandler<SuspendInventoryCommand, InventoryItemResult> {

    private final SuspendInventoryUseCase suspendInventoryUseCase;

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
