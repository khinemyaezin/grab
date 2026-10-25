package com.grab.app.productmerchandising.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.productmerchandising.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.ReturnToVendorCommand;
import com.inventory.application.port.inbound.ReturnToVendorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnToVendorCommandHandler implements CommandHandler<ReturnToVendorCommand, InventoryItemResult> {

    private final ReturnToVendorUseCase returnToVendorUseCase;

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
