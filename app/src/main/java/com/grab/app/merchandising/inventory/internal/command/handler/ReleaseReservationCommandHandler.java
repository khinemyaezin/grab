package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ReleaseReservationCommand;
import com.inventory.application.model.write.InventoryReservationResult;
import com.inventory.application.port.inbound.ReleaseReservationUseCase;
import org.springframework.stereotype.Component;

@Component
public class ReleaseReservationCommandHandler implements CommandHandler<ReleaseReservationCommand, InventoryReservationResult> {

    private final ReleaseReservationUseCase releaseReservationUseCase;

    public ReleaseReservationCommandHandler(ReleaseReservationUseCase releaseReservationUseCase) {
        this.releaseReservationUseCase = releaseReservationUseCase;
    }

    @Override
    @InventoryTransactional
    public InventoryReservationResult handle(ReleaseReservationCommand command) {
        return releaseReservationUseCase.execute(command);
    }

    @Override
    public Class<ReleaseReservationCommand> getCommandType() {
        return ReleaseReservationCommand.class;
    }
}
