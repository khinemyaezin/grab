package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.AllocateStockCommand;
import com.inventory.application.model.write.AllocateStockResult;
import com.inventory.application.port.inbound.AllocateStockUseCase;
import org.springframework.stereotype.Component;

@Component
public class AllocateStockCommandHandler implements CommandHandler<AllocateStockCommand, AllocateStockResult> {

    private final AllocateStockUseCase allocateStockUseCase;

    public AllocateStockCommandHandler(AllocateStockUseCase allocateStockUseCase) {
        this.allocateStockUseCase = allocateStockUseCase;
    }

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
