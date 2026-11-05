package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.DeleteBinCommand;

import com.inventory.application.port.inbound.DeleteBinUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeleteBinCommandHandler implements CommandHandler<DeleteBinCommand, Void> {

    private final DeleteBinUseCase deleteBinUseCase;

    public DeleteBinCommandHandler(DeleteBinUseCase deleteBinUseCase) {
        this.deleteBinUseCase = deleteBinUseCase;
    }

    @Override
    @InventoryTransactional
    public Void handle(DeleteBinCommand command) {
        return deleteBinUseCase.execute(command);
    }

    @Override
    public Class<DeleteBinCommand> getCommandType() {
        return DeleteBinCommand.class;
    }
}
