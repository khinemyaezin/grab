package com.grab.service.channel.saleschannel.internal.command.handler;

import com.grab.service.channel.saleschannel.internal.config.SalesChannelTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.saleschannel.application.model.write.ProvisionMerchantWebsiteChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;
import com.saleschannel.application.port.inbound.ProvisionMerchantWebsiteChannelUseCase;
import org.springframework.stereotype.Component;

@Component
public class ProvisionMerchantWebsiteChannelCommandHandler
        implements CommandHandler<ProvisionMerchantWebsiteChannelCommand, ProvisionSalesChannelResult> {

    private final ProvisionMerchantWebsiteChannelUseCase useCase;

    public ProvisionMerchantWebsiteChannelCommandHandler(ProvisionMerchantWebsiteChannelUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SalesChannelTransactional
    public ProvisionSalesChannelResult handle(ProvisionMerchantWebsiteChannelCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<ProvisionMerchantWebsiteChannelCommand> getCommandType() {
        return ProvisionMerchantWebsiteChannelCommand.class;
    }
}

