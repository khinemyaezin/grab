package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.DeactivateLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.port.inbound.DeactivateLocationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateLocationCommandHandler implements CommandHandler<DeactivateLocationCommand, LocationResult> {

    private final DeactivateLocationUseCase deactivateLocationUseCase;

    @Override
    @InventoryTransactional
    public LocationResult handle(DeactivateLocationCommand command) {
        return deactivateLocationUseCase.execute(command);
    }

    @Override
    public Class<DeactivateLocationCommand> getCommandType() {
        return DeactivateLocationCommand.class;
    }
}
