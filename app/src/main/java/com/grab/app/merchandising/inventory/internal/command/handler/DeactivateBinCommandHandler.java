package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.DeactivateBinCommand;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.port.inbound.DeactivateBinUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeactivateBinCommandHandler implements CommandHandler<DeactivateBinCommand, BinResult> {

    private final DeactivateBinUseCase deactivateBinUseCase;

    public DeactivateBinCommandHandler(DeactivateBinUseCase deactivateBinUseCase) {
        this.deactivateBinUseCase = deactivateBinUseCase;
    }

    @Override
    @InventoryTransactional
    public BinResult handle(DeactivateBinCommand command) {
        return deactivateBinUseCase.execute(command);
    }

    @Override
    public Class<DeactivateBinCommand> getCommandType() {
        return DeactivateBinCommand.class;
    }
}
