package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.ApplyProductStatusCommand;
import com.catalog.application.model.write.ApplyProductStatusResult;
import com.catalog.application.port.inbound.ApplyProductStatusUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplyProductStatusCommandHandler implements CommandHandler<ApplyProductStatusCommand, ApplyProductStatusResult> {

    private final ApplyProductStatusUseCase applyProductStatusUseCase;

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
