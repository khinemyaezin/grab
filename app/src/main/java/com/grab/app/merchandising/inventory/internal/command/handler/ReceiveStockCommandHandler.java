package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ReceiveStockCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.ReceiveStockUseCase;
import org.springframework.stereotype.Component;

@Component
public class ReceiveStockCommandHandler implements CommandHandler<ReceiveStockCommand, InventoryItemResult> {

    private final ReceiveStockUseCase receiveStockUseCase;

    public ReceiveStockCommandHandler(ReceiveStockUseCase receiveStockUseCase) {
        this.receiveStockUseCase = receiveStockUseCase;
    }

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(ReceiveStockCommand command) {
        return receiveStockUseCase.execute(command);
    }

    @Override
    public Class<ReceiveStockCommand> getCommandType() {
        return ReceiveStockCommand.class;
    }
}
