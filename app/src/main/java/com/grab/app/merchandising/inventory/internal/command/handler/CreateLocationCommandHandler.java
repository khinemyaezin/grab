package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.CreateLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.port.inbound.CreateLocationUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreateLocationCommandHandler implements CommandHandler<CreateLocationCommand, LocationResult> {

    private final CreateLocationUseCase createLocationUseCase;

    public CreateLocationCommandHandler(CreateLocationUseCase createLocationUseCase) {
        this.createLocationUseCase = createLocationUseCase;
    }

    @Override
    @InventoryTransactional
    public LocationResult handle(CreateLocationCommand command) {
        return createLocationUseCase.execute(command);
    }

    @Override
    public Class<CreateLocationCommand> getCommandType() {
        return CreateLocationCommand.class;
    }
}
