package com.grab.store.storefrontquery.internal.command.handler;

import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;
import com.grab.discovery.application.model.write.RebuildProgressResult;
import com.grab.discovery.application.port.inbound.RebuildDiscoveryProductsUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.storefrontquery.internal.config.StorefrontQueryTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RebuildDiscoveryProductsCommandHandler implements CommandHandler<RebuildDiscoveryProductsCommand, RebuildProgressResult> {

    private final RebuildDiscoveryProductsUseCase rebuildDiscoveryProductsUseCase;

    @Override
    @StorefrontQueryTransactional
    public RebuildProgressResult handle(RebuildDiscoveryProductsCommand command) {
        return rebuildDiscoveryProductsUseCase.execute(command);
    }

    @Override
    public Class<RebuildDiscoveryProductsCommand> getCommandType() {
        return RebuildDiscoveryProductsCommand.class;
    }
}
