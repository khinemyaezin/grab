package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ReceiveInTransitCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.ReceiveInTransitUseCase;
import org.springframework.stereotype.Component;

@Component
public class ReceiveInTransitCommandHandler implements CommandHandler<ReceiveInTransitCommand, InventoryItemResult> {

    private final ReceiveInTransitUseCase receiveInTransitUseCase;

    public ReceiveInTransitCommandHandler(ReceiveInTransitUseCase receiveInTransitUseCase) {
        this.receiveInTransitUseCase = receiveInTransitUseCase;
    }

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(ReceiveInTransitCommand command) {
        return receiveInTransitUseCase.execute(command);
    }

    @Override
    public Class<ReceiveInTransitCommand> getCommandType() {
        return ReceiveInTransitCommand.class;
    }
}
