package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ReturnToVendorCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.ReturnToVendorUseCase;
import org.springframework.stereotype.Component;

@Component
public class ReturnToVendorCommandHandler implements CommandHandler<ReturnToVendorCommand, InventoryItemResult> {

    private final ReturnToVendorUseCase returnToVendorUseCase;

    public ReturnToVendorCommandHandler(ReturnToVendorUseCase returnToVendorUseCase) {
        this.returnToVendorUseCase = returnToVendorUseCase;
    }

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(ReturnToVendorCommand command) {
        return returnToVendorUseCase.execute(command);
    }

    @Override
    public Class<ReturnToVendorCommand> getCommandType() {
        return ReturnToVendorCommand.class;
    }
}
