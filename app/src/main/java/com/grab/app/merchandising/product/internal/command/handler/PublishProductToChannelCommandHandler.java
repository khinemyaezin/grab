package com.grab.app.merchandising.product.internal.command.handler;

import com.catalog.application.model.write.PublishProductToChannelCommand;
import com.catalog.application.model.write.PublishProductToChannelResult;
import com.catalog.application.port.inbound.PublishProductToChannelUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class PublishProductToChannelCommandHandler implements CommandHandler<PublishProductToChannelCommand, PublishProductToChannelResult> {

    private final PublishProductToChannelUseCase publishProductToChannelUseCase;

    public PublishProductToChannelCommandHandler(PublishProductToChannelUseCase publishProductToChannelUseCase) {
        this.publishProductToChannelUseCase = publishProductToChannelUseCase;
    }

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
