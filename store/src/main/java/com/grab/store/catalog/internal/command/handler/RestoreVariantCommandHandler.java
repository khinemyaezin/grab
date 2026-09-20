package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.command.RestoreVariantCommand;
import com.catalog.application.command.RestoreVariantResult;
import com.catalog.application.port.inbound.RestoreVariantUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestoreVariantCommandHandler implements CommandHandler<RestoreVariantCommand, RestoreVariantResult> {

    private final RestoreVariantUseCase restoreVariantUseCase;

    @Override
    @CatalogTransactional
    public RestoreVariantResult handle(RestoreVariantCommand command) {
        return restoreVariantUseCase.execute(command);
    }

    @Override
    public Class<RestoreVariantCommand> getCommandType() {
        return RestoreVariantCommand.class;
    }
}
