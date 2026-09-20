package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.command.PublishProductToChannelCommand;
import com.catalog.application.command.PublishProductToChannelResult;
import com.catalog.application.port.inbound.PublishProductToChannelUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublishProductToChannelCommandHandler implements CommandHandler<PublishProductToChannelCommand, PublishProductToChannelResult> {

    private final PublishProductToChannelUseCase publishProductToChannelUseCase;

    @Override
    @CatalogTransactional
    public PublishProductToChannelResult handle(PublishProductToChannelCommand command) {
        return publishProductToChannelUseCase.execute(command);
    }

    @Override
    public Class<PublishProductToChannelCommand> getCommandType() {
        return PublishProductToChannelCommand.class;
    }
}
