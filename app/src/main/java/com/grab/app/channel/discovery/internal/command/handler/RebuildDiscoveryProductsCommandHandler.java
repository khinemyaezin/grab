package com.grab.app.channel.discovery.internal.command.handler;

import com.grab.app.channel.discovery.internal.config.DiscoveryTransactional;
import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;
import com.grab.discovery.application.model.write.RebuildProgressResult;
import com.grab.discovery.application.port.inbound.RebuildDiscoveryProductsUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class RebuildDiscoveryProductsCommandHandler
        implements CommandHandler<RebuildDiscoveryProductsCommand, RebuildProgressResult> {

    private final RebuildDiscoveryProductsUseCase useCase;

    public RebuildDiscoveryProductsCommandHandler(RebuildDiscoveryProductsUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @DiscoveryTransactional(propagation = Propagation.REQUIRES_NEW)
    public RebuildProgressResult handle(RebuildDiscoveryProductsCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<RebuildDiscoveryProductsCommand> getCommandType() {
        return RebuildDiscoveryProductsCommand.class;
    }
}

