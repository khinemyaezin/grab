package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.WriteOffStockCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.WriteOffStockUseCase;
import org.springframework.stereotype.Component;

@Component
public class WriteOffStockCommandHandler implements CommandHandler<WriteOffStockCommand, InventoryItemResult> {

    private final WriteOffStockUseCase writeOffStockUseCase;

    public WriteOffStockCommandHandler(WriteOffStockUseCase writeOffStockUseCase) {
        this.writeOffStockUseCase = writeOffStockUseCase;
    }

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
