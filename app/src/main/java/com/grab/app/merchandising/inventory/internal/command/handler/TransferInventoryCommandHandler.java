package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.TransferInventoryCommand;
import com.inventory.application.model.write.TransferInventoryResult;
import com.inventory.application.port.inbound.TransferInventoryUseCase;
import org.springframework.stereotype.Component;

@Component
public class TransferInventoryCommandHandler implements CommandHandler<TransferInventoryCommand, TransferInventoryResult> {

    private final TransferInventoryUseCase transferInventoryUseCase;

    public TransferInventoryCommandHandler(TransferInventoryUseCase transferInventoryUseCase) {
        this.transferInventoryUseCase = transferInventoryUseCase;
    }

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
