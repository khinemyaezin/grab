package com.grab.app.channeldiscovery.saleschannel.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.channeldiscovery.saleschannel.internal.config.SalesChannelProvisioningRetryable;
import com.grab.app.channeldiscovery.saleschannel.internal.config.SalesChannelTransactional;
import com.saleschannel.application.model.write.ProvisionMerchantWebsiteChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;
import com.saleschannel.application.port.inbound.ProvisionMerchantWebsiteChannelUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class ProvisionMerchantWebsiteChannelCommandHandler
        implements CommandHandler<ProvisionMerchantWebsiteChannelCommand, ProvisionSalesChannelResult> {
    private final ProvisionMerchantWebsiteChannelUseCase useCase;

    @Override
    @SalesChannelProvisioningRetryable
    @SalesChannelTransactional(propagation = Propagation.REQUIRES_NEW)
    public ProvisionSalesChannelResult handle(ProvisionMerchantWebsiteChannelCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<ProvisionMerchantWebsiteChannelCommand> getCommandType() {
        return ProvisionMerchantWebsiteChannelCommand.class;
    }
}
