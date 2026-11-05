package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ActivateBinCommand;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.port.inbound.ActivateBinUseCase;
import org.springframework.stereotype.Component;

@Component
public class ActivateBinCommandHandler implements CommandHandler<ActivateBinCommand, BinResult> {

    private final ActivateBinUseCase activateBinUseCase;

    public ActivateBinCommandHandler(ActivateBinUseCase activateBinUseCase) {
        this.activateBinUseCase = activateBinUseCase;
    }

    @Override
    @InventoryTransactional
    public BinResult handle(ActivateBinCommand command) {
        return activateBinUseCase.execute(command);
    }

    @Override
    public Class<ActivateBinCommand> getCommandType() {
        return ActivateBinCommand.class;
    }
}
