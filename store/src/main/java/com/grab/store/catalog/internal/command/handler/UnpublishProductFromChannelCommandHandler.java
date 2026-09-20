package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.UnpublishProductFromChannelCommand;
import com.catalog.application.model.write.UnpublishProductFromChannelResult;
import com.catalog.application.port.inbound.UnpublishProductFromChannelUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnpublishProductFromChannelCommandHandler implements CommandHandler<UnpublishProductFromChannelCommand, UnpublishProductFromChannelResult> {

    private final UnpublishProductFromChannelUseCase unpublishProductFromChannelUseCase;

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
