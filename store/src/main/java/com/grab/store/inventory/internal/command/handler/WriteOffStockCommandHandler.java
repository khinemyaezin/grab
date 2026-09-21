package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.WriteOffStockCommand;
import com.inventory.application.port.inbound.WriteOffStockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WriteOffStockCommandHandler implements CommandHandler<WriteOffStockCommand, InventoryItemResult> {

    private final WriteOffStockUseCase writeOffStockUseCase;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(WriteOffStockCommand command) {
        return writeOffStockUseCase.execute(command);
    }

    @Override
    public Class<WriteOffStockCommand> getCommandType() {
        return WriteOffStockCommand.class;
    }
}
