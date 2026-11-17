package com.grab.service.channel.saleschannel.internal.command.handler;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.grab.service.channel.saleschannel.internal.config.SalesChannelTransactional;
import com.grab.service.channel.shared.security.SecurityManifestPublicationRetryable;
import com.grab.framework.cqrs.command.CommandHandler;
import com.saleschannel.application.model.write.PublishSalesChannelSecurityManifestCommand;
import com.saleschannel.application.port.inbound.PublishSalesChannelSecurityManifestUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class PublishSalesChannelSecurityManifestCommandHandler
        implements CommandHandler<PublishSalesChannelSecurityManifestCommand, PublicationResult> {

    private final PublishSalesChannelSecurityManifestUseCase useCase;

    public PublishSalesChannelSecurityManifestCommandHandler(PublishSalesChannelSecurityManifestUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SecurityManifestPublicationRetryable
    @SalesChannelTransactional(propagation = Propagation.REQUIRES_NEW)
    public PublicationResult handle(PublishSalesChannelSecurityManifestCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<PublishSalesChannelSecurityManifestCommand> getCommandType() {
        return PublishSalesChannelSecurityManifestCommand.class;
    }
}

