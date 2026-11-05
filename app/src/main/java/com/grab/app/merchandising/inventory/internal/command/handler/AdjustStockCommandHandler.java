package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.AdjustStockCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.AdjustStockUseCase;
import org.springframework.stereotype.Component;

@Component
public class AdjustStockCommandHandler implements CommandHandler<AdjustStockCommand, InventoryItemResult> {

    private final AdjustStockUseCase adjustStockUseCase;

    public AdjustStockCommandHandler(AdjustStockUseCase adjustStockUseCase) {
        this.adjustStockUseCase = adjustStockUseCase;
    }

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
