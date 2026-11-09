package com.grab.app.channel.saleschannel.internal.command.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelTransactional;
import com.grab.app.shared.security.SecurityManifestPublicationRetryable;
import com.grab.framework.cqrs.command.CommandHandler;
import com.saleschannel.application.model.write.PublishSalesChannelSecurityManifestCommand;
import com.saleschannel.application.port.inbound.PublishSalesChannelSecurityManifestUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class PublishSalesChannelSecurityManifestCommandHandler
        implements CommandHandler<PublishSalesChannelSecurityManifestCommand, Void> {

    private final PublishSalesChannelSecurityManifestUseCase useCase;

    public PublishSalesChannelSecurityManifestCommandHandler(PublishSalesChannelSecurityManifestUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SecurityManifestPublicationRetryable
    @SalesChannelTransactional(propagation = Propagation.REQUIRES_NEW)
    public Void handle(PublishSalesChannelSecurityManifestCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<PublishSalesChannelSecurityManifestCommand> getCommandType() {
        return PublishSalesChannelSecurityManifestCommand.class;
    }
}

