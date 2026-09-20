package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.InventoryReservationResult;
import com.inventory.application.model.write.ShipReservationCommand;
import com.inventory.application.port.inbound.ShipReservationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipReservationCommandHandler implements CommandHandler<ShipReservationCommand, InventoryReservationResult> {

    private final ShipReservationUseCase shipReservationUseCase;

    @Override
    @InventoryTransactional
    public InventoryReservationResult handle(ShipReservationCommand command) {
        return shipReservationUseCase.execute(command);
    }

    @Override
    public Class<ShipReservationCommand> getCommandType() {
        return ShipReservationCommand.class;
    }
}
