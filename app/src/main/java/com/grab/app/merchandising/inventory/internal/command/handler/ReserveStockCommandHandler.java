package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ReserveStockCommand;
import com.inventory.application.model.write.InventoryReservationResult;
import com.inventory.application.port.inbound.ReserveStockUseCase;
import org.springframework.stereotype.Component;

@Component
public class ReserveStockCommandHandler implements CommandHandler<ReserveStockCommand, InventoryReservationResult> {

    private final ReserveStockUseCase reserveStockUseCase;

    public ReserveStockCommandHandler(ReserveStockUseCase reserveStockUseCase) {
        this.reserveStockUseCase = reserveStockUseCase;
    }

    @Override
    @InventoryTransactional
    public InventoryReservationResult handle(ReserveStockCommand command) {
        return reserveStockUseCase.execute(command);
    }

    @Override
    public Class<ReserveStockCommand> getCommandType() {
        return ReserveStockCommand.class;
    }
}
