package com.grab.store.saleschannel.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.saleschannel.internal.config.SalesChannelTransactional;
import com.saleschannel.application.model.write.PublishSalesChannelSecurityManifestCommand;
import com.saleschannel.application.port.inbound.PublishSalesChannelSecurityManifestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublishSalesChannelSecurityManifestCommandHandler
        implements CommandHandler<PublishSalesChannelSecurityManifestCommand, Void> {
    private final PublishSalesChannelSecurityManifestUseCase useCase;

    @Override
    @SalesChannelTransactional
    public Void handle(PublishSalesChannelSecurityManifestCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<PublishSalesChannelSecurityManifestCommand> getCommandType() {
        return PublishSalesChannelSecurityManifestCommand.class;
    }
}
