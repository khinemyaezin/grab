package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.UpdateLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.port.inbound.UpdateLocationUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdateLocationCommandHandler implements CommandHandler<UpdateLocationCommand, LocationResult> {

    private final UpdateLocationUseCase updateLocationUseCase;

    public UpdateLocationCommandHandler(UpdateLocationUseCase updateLocationUseCase) {
        this.updateLocationUseCase = updateLocationUseCase;
    }

    @Override
    @InventoryTransactional
    public LocationResult handle(UpdateLocationCommand command) {
        return updateLocationUseCase.execute(command);
    }

    @Override
    public Class<UpdateLocationCommand> getCommandType() {
        return UpdateLocationCommand.class;
    }
}
