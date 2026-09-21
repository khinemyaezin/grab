package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.DeleteBinCommand;
import com.inventory.application.port.inbound.DeleteBinUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteBinCommandHandler implements CommandHandler<DeleteBinCommand, Void> {

    private final DeleteBinUseCase deleteBinUseCase;

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
