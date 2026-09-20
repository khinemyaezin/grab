package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.AllocateStockCommand;
import com.inventory.application.model.write.AllocateStockResult;
import com.inventory.application.port.inbound.AllocateStockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AllocateStockCommandHandler implements CommandHandler<AllocateStockCommand, AllocateStockResult> {

    private final AllocateStockUseCase allocateStockUseCase;

    @Override
    @InventoryTransactional
    public AllocateStockResult handle(AllocateStockCommand command) {
        return allocateStockUseCase.execute(command);
    }

    @Override
    public Class<AllocateStockCommand> getCommandType() {
        return AllocateStockCommand.class;
    }
}
