package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.AnnounceInTransitCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.AnnounceInTransitUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnnounceInTransitCommandHandler implements CommandHandler<AnnounceInTransitCommand, InventoryItemResult> {

    private final AnnounceInTransitUseCase announceInTransitUseCase;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(AnnounceInTransitCommand command) {
        return announceInTransitUseCase.execute(command);
    }

    @Override
    public Class<AnnounceInTransitCommand> getCommandType() {
        return AnnounceInTransitCommand.class;
    }
}
