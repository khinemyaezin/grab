package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.MarkDamagedCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.MarkDamagedUseCase;
import org.springframework.stereotype.Component;

@Component
public class MarkDamagedCommandHandler implements CommandHandler<MarkDamagedCommand, InventoryItemResult> {

    private final MarkDamagedUseCase markDamagedUseCase;

    public MarkDamagedCommandHandler(MarkDamagedUseCase markDamagedUseCase) {
        this.markDamagedUseCase = markDamagedUseCase;
    }

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(MarkDamagedCommand command) {
        return markDamagedUseCase.execute(command);
    }

    @Override
    public Class<MarkDamagedCommand> getCommandType() {
        return MarkDamagedCommand.class;
    }
}
