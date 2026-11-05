package com.grab.app.merchandising.product.internal.command.handler;

import com.catalog.application.model.write.UnpublishProductFromChannelCommand;
import com.catalog.application.model.write.UnpublishProductFromChannelResult;
import com.catalog.application.port.inbound.UnpublishProductFromChannelUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UnpublishProductFromChannelCommandHandler implements CommandHandler<UnpublishProductFromChannelCommand, UnpublishProductFromChannelResult> {

    private final UnpublishProductFromChannelUseCase unpublishProductFromChannelUseCase;

    public UnpublishProductFromChannelCommandHandler(UnpublishProductFromChannelUseCase unpublishProductFromChannelUseCase) {
        this.unpublishProductFromChannelUseCase = unpublishProductFromChannelUseCase;
    }

    @Override
    @CatalogTransactional
    public UnpublishProductFromChannelResult handle(UnpublishProductFromChannelCommand command) {
        return unpublishProductFromChannelUseCase.execute(command);
    }

    @Override
    public Class<UnpublishProductFromChannelCommand> getCommandType() {
        return UnpublishProductFromChannelCommand.class;
    }
}
