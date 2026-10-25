package com.grab.app.channeldiscovery.saleschannel.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.channeldiscovery.saleschannel.internal.config.SalesChannelTransactional;
import com.saleschannel.application.model.write.ApplySalesChannelRegionBindingsCommand;
import com.saleschannel.application.port.inbound.ApplySalesChannelRegionBindingsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplySalesChannelRegionBindingsCommandHandler
        implements CommandHandler<ApplySalesChannelRegionBindingsCommand, Integer> {
    private final ApplySalesChannelRegionBindingsUseCase useCase;

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
