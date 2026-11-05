package com.grab.app.merchandising.product.internal.command.handler;

import com.catalog.application.model.write.RestoreVariantCommand;
import com.catalog.application.model.write.RestoreVariantResult;
import com.catalog.application.port.inbound.RestoreVariantUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class RestoreVariantCommandHandler implements CommandHandler<RestoreVariantCommand, RestoreVariantResult> {

    private final RestoreVariantUseCase restoreVariantUseCase;

    public RestoreVariantCommandHandler(RestoreVariantUseCase restoreVariantUseCase) {
        this.restoreVariantUseCase = restoreVariantUseCase;
    }

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
