package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.MarkDamagedCommand;
import com.inventory.application.port.inbound.MarkDamagedUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarkDamagedCommandHandler implements CommandHandler<MarkDamagedCommand, InventoryItemResult> {

    private final MarkDamagedUseCase markDamagedUseCase;

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
