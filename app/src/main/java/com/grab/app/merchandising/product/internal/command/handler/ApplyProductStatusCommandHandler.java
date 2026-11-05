package com.grab.app.merchandising.product.internal.command.handler;

import com.catalog.application.model.write.ApplyProductStatusCommand;
import com.catalog.application.model.write.ApplyProductStatusResult;
import com.catalog.application.port.inbound.ApplyProductStatusUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ApplyProductStatusCommandHandler implements CommandHandler<ApplyProductStatusCommand, ApplyProductStatusResult> {

    private final ApplyProductStatusUseCase applyProductStatusUseCase;

    public ApplyProductStatusCommandHandler(ApplyProductStatusUseCase applyProductStatusUseCase) {
        this.applyProductStatusUseCase = applyProductStatusUseCase;
    }

    @Override
    @CatalogTransactional
    public ApplyProductStatusResult handle(ApplyProductStatusCommand command) {
        return applyProductStatusUseCase.execute(command);
    }

    @Override
    public Class<ApplyProductStatusCommand> getCommandType() {
        return ApplyProductStatusCommand.class;
    }
}
