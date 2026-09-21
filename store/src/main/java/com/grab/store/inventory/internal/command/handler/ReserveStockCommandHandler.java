package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.InventoryReservationResult;
import com.inventory.application.model.write.ReserveStockCommand;
import com.inventory.application.port.inbound.ReserveStockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReserveStockCommandHandler implements CommandHandler<ReserveStockCommand, InventoryReservationResult> {

    private final ReserveStockUseCase reserveStockUseCase;

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
