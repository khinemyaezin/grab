package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.AnnounceInTransitCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.AnnounceInTransitUseCase;
import org.springframework.stereotype.Component;

@Component
public class AnnounceInTransitCommandHandler implements CommandHandler<AnnounceInTransitCommand, InventoryItemResult> {

    private final AnnounceInTransitUseCase announceInTransitUseCase;

    public AnnounceInTransitCommandHandler(AnnounceInTransitUseCase announceInTransitUseCase) {
        this.announceInTransitUseCase = announceInTransitUseCase;
    }

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
