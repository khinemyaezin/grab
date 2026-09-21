package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.AdjustStockCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.AdjustStockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdjustStockCommandHandler implements CommandHandler<AdjustStockCommand, InventoryItemResult> {

    private final AdjustStockUseCase adjustStockUseCase;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(AdjustStockCommand command) {
        return adjustStockUseCase.execute(command);
    }

    @Override
    public Class<AdjustStockCommand> getCommandType() {
        return AdjustStockCommand.class;
    }
}
