package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.ReceiveStockCommand;
import com.inventory.application.port.inbound.ReceiveStockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReceiveStockCommandHandler implements CommandHandler<ReceiveStockCommand, InventoryItemResult> {

    private final ReceiveStockUseCase receiveStockUseCase;

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
