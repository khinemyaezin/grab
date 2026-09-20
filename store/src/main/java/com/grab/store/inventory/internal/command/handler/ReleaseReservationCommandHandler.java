package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.InventoryReservationResult;
import com.inventory.application.model.write.ReleaseReservationCommand;
import com.inventory.application.port.inbound.ReleaseReservationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReleaseReservationCommandHandler implements CommandHandler<ReleaseReservationCommand, InventoryReservationResult> {

    private final ReleaseReservationUseCase releaseReservationUseCase;

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
