package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.DeactivateLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.port.inbound.DeactivateLocationUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeactivateLocationCommandHandler implements CommandHandler<DeactivateLocationCommand, LocationResult> {

    private final DeactivateLocationUseCase deactivateLocationUseCase;

    public DeactivateLocationCommandHandler(DeactivateLocationUseCase deactivateLocationUseCase) {
        this.deactivateLocationUseCase = deactivateLocationUseCase;
    }

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
