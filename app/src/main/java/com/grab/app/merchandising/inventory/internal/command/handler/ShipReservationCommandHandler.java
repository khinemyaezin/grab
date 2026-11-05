package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ShipReservationCommand;
import com.inventory.application.model.write.InventoryReservationResult;
import com.inventory.application.port.inbound.ShipReservationUseCase;
import org.springframework.stereotype.Component;

@Component
public class ShipReservationCommandHandler implements CommandHandler<ShipReservationCommand, InventoryReservationResult> {

    private final ShipReservationUseCase shipReservationUseCase;

    public ShipReservationCommandHandler(ShipReservationUseCase shipReservationUseCase) {
        this.shipReservationUseCase = shipReservationUseCase;
    }

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
