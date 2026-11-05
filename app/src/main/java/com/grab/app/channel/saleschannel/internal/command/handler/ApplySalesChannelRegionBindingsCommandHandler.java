package com.grab.app.channel.saleschannel.internal.command.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.saleschannel.application.model.write.ApplySalesChannelRegionBindingsCommand;
import com.saleschannel.application.port.inbound.ApplySalesChannelRegionBindingsUseCase;
import org.springframework.stereotype.Component;

@Component
public class ApplySalesChannelRegionBindingsCommandHandler
        implements CommandHandler<ApplySalesChannelRegionBindingsCommand, Integer> {

    private final ApplySalesChannelRegionBindingsUseCase useCase;

    public ApplySalesChannelRegionBindingsCommandHandler(ApplySalesChannelRegionBindingsUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SalesChannelTransactional
    public Integer handle(ApplySalesChannelRegionBindingsCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<ApplySalesChannelRegionBindingsCommand> getCommandType() {
        return ApplySalesChannelRegionBindingsCommand.class;
    }
}

