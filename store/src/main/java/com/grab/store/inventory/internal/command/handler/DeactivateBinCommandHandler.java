package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.model.write.DeactivateBinCommand;
import com.inventory.application.port.inbound.DeactivateBinUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateBinCommandHandler implements CommandHandler<DeactivateBinCommand, BinResult> {

    private final DeactivateBinUseCase deactivateBinUseCase;

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
