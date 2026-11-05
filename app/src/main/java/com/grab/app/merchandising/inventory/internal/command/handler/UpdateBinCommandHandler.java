package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.UpdateBinCommand;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.port.inbound.UpdateBinUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdateBinCommandHandler implements CommandHandler<UpdateBinCommand, BinResult> {

    private final UpdateBinUseCase updateBinUseCase;

    public UpdateBinCommandHandler(UpdateBinUseCase updateBinUseCase) {
        this.updateBinUseCase = updateBinUseCase;
    }

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
