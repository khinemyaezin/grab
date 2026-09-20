package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.TransferInventoryCommand;
import com.inventory.application.model.write.TransferInventoryResult;
import com.inventory.application.port.inbound.TransferInventoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferInventoryCommandHandler implements CommandHandler<TransferInventoryCommand, TransferInventoryResult> {

    private final TransferInventoryUseCase transferInventoryUseCase;

    @Override
    @InventoryTransactional
    public TransferInventoryResult handle(TransferInventoryCommand command) {
        return transferInventoryUseCase.execute(command);
    }

    @Override
    public Class<TransferInventoryCommand> getCommandType() {
        return TransferInventoryCommand.class;
    }
}
