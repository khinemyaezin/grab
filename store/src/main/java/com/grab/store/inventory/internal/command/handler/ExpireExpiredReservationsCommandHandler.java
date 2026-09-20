package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.ExpireExpiredReservationsCommand;
import com.inventory.application.model.write.ExpireExpiredReservationsResult;
import com.inventory.application.port.inbound.ExpireExpiredReservationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpireExpiredReservationsCommandHandler implements CommandHandler<ExpireExpiredReservationsCommand, ExpireExpiredReservationsResult> {

    private final ExpireExpiredReservationsUseCase expireExpiredReservationsUseCase;

    @Override
    @InventoryTransactional
    public ExpireExpiredReservationsResult handle(ExpireExpiredReservationsCommand command) {
        return expireExpiredReservationsUseCase.execute(command);
    }

    @Override
    public Class<ExpireExpiredReservationsCommand> getCommandType() {
        return ExpireExpiredReservationsCommand.class;
    }
}
