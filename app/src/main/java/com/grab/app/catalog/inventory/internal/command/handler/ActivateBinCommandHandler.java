package com.grab.app.catalog.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.catalog.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.ActivateBinCommand;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.port.inbound.ActivateBinUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivateBinCommandHandler implements CommandHandler<ActivateBinCommand, BinResult> {

    private final ActivateBinUseCase activateBinUseCase;

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
