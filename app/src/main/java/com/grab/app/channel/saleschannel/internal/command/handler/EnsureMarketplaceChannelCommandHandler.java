package com.grab.app.channel.saleschannel.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.channel.saleschannel.internal.config.SalesChannelProvisioningRetryable;
import com.grab.app.channel.saleschannel.internal.config.SalesChannelTransactional;
import com.saleschannel.application.model.write.EnsureMarketplaceChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;
import com.saleschannel.application.port.inbound.EnsureMarketplaceChannelUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class EnsureMarketplaceChannelCommandHandler
        implements CommandHandler<EnsureMarketplaceChannelCommand, ProvisionSalesChannelResult> {
    private final EnsureMarketplaceChannelUseCase useCase;

    @Override
    @SalesChannelProvisioningRetryable
    @SalesChannelTransactional(propagation = Propagation.REQUIRES_NEW)
    public ProvisionSalesChannelResult handle(EnsureMarketplaceChannelCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<EnsureMarketplaceChannelCommand> getCommandType() {
        return EnsureMarketplaceChannelCommand.class;
    }
}
