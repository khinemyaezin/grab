package com.grab.store.storefrontquery.internal.command.handler;

import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;
import com.grab.discovery.application.model.write.RebuildProgressResult;
import com.grab.discovery.application.port.inbound.RebuildDiscoveryProductsUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.storefrontquery.internal.config.DiscoveryProjectionRetryable;
import com.grab.store.storefrontquery.internal.config.StorefrontQueryTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class RebuildDiscoveryProductsCommandHandler implements CommandHandler<RebuildDiscoveryProductsCommand, RebuildProgressResult> {

    private final RebuildDiscoveryProductsUseCase useCase;

    @Override
    @DiscoveryProjectionRetryable
    @StorefrontQueryTransactional(propagation = Propagation.REQUIRES_NEW)
    public RebuildProgressResult handle(RebuildDiscoveryProductsCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<RebuildDiscoveryProductsCommand> getCommandType() {
        return RebuildDiscoveryProductsCommand.class;
    }
}
