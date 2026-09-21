package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.model.write.UpdateBinCommand;
import com.inventory.application.port.inbound.UpdateBinUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateBinCommandHandler implements CommandHandler<UpdateBinCommand, BinResult> {

    private final UpdateBinUseCase updateBinUseCase;

    @Override
    @InventoryTransactional
    public BinResult handle(UpdateBinCommand command) {
        return updateBinUseCase.execute(command);
    }

    @Override
    public Class<UpdateBinCommand> getCommandType() {
        return UpdateBinCommand.class;
    }
}
