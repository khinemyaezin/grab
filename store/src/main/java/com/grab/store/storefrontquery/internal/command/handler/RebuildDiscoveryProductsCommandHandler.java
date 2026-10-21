package com.grab.store.storefrontquery.internal.command.handler;

import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;
import com.grab.discovery.application.port.inbound.RebuildDiscoveryProductsUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.storefrontquery.internal.config.StorefrontQueryTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RebuildDiscoveryProductsCommandHandler implements CommandHandler<RebuildDiscoveryProductsCommand, Void> {
    private final RebuildDiscoveryProductsUseCase rebuildDiscoveryProductsUseCase;

    @Override
    @StorefrontQueryTransactional
    public Void handle(RebuildDiscoveryProductsCommand command) {
        rebuildDiscoveryProductsUseCase.execute(command);
        return null;
    }

    @Override
    public Class<RebuildDiscoveryProductsCommand> getCommandType() {
        return RebuildDiscoveryProductsCommand.class;
    }
}
