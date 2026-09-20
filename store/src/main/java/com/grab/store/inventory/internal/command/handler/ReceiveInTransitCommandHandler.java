package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.ReceiveInTransitCommand;
import com.inventory.application.port.inbound.ReceiveInTransitUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReceiveInTransitCommandHandler implements CommandHandler<ReceiveInTransitCommand, InventoryItemResult> {

    private final ReceiveInTransitUseCase receiveInTransitUseCase;

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
