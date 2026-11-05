package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ExpireExpiredReservationsCommand;
import com.inventory.application.model.write.ExpireExpiredReservationsResult;
import com.inventory.application.port.inbound.ExpireExpiredReservationsUseCase;
import org.springframework.stereotype.Component;

@Component
public class ExpireExpiredReservationsCommandHandler implements CommandHandler<ExpireExpiredReservationsCommand, ExpireExpiredReservationsResult> {

    private final ExpireExpiredReservationsUseCase expireExpiredReservationsUseCase;

    public ExpireExpiredReservationsCommandHandler(ExpireExpiredReservationsUseCase expireExpiredReservationsUseCase) {
        this.expireExpiredReservationsUseCase = expireExpiredReservationsUseCase;
    }

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
