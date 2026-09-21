package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.DeallocateStockCommand;
import com.inventory.application.model.write.DeallocateStockResult;
import com.inventory.application.port.inbound.DeallocateStockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeallocateStockCommandHandler implements CommandHandler<DeallocateStockCommand, DeallocateStockResult> {

    private final DeallocateStockUseCase deallocateStockUseCase;

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
