package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.DeallocateStockCommand;
import com.inventory.application.model.write.DeallocateStockResult;
import com.inventory.application.port.inbound.DeallocateStockUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeallocateStockCommandHandler implements CommandHandler<DeallocateStockCommand, DeallocateStockResult> {

    private final DeallocateStockUseCase deallocateStockUseCase;

    public DeallocateStockCommandHandler(DeallocateStockUseCase deallocateStockUseCase) {
        this.deallocateStockUseCase = deallocateStockUseCase;
    }

    @Override
    @InventoryTransactional
    public DeallocateStockResult handle(DeallocateStockCommand command) {
        return deallocateStockUseCase.execute(command);
    }

    @Override
    public Class<DeallocateStockCommand> getCommandType() {
        return DeallocateStockCommand.class;
    }
}
