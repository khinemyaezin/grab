package com.grab.store.storefrontquery.internal.command.handler;

import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;
import com.grab.discovery.application.port.inbound.ProjectDiscoveryProductUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.storefrontquery.internal.config.DiscoveryProjectionRetryable;
import com.grab.store.storefrontquery.internal.config.StorefrontQueryTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class ProjectDiscoveryProductCommandHandler implements CommandHandler<ProjectDiscoveryProductCommand, Void> {

    private final ProjectDiscoveryProductUseCase useCase;

    @Override
    @DiscoveryProjectionRetryable
    @StorefrontQueryTransactional(propagation = Propagation.REQUIRES_NEW)
    public Void handle(ProjectDiscoveryProductCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<ProjectDiscoveryProductCommand> getCommandType() {
        return ProjectDiscoveryProductCommand.class;
    }
}
