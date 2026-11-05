package com.grab.app.channel.discovery.internal.command.handler;

import com.grab.app.channel.discovery.internal.config.DiscoveryTransactional;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;
import com.grab.discovery.application.port.inbound.ProjectDiscoveryProductUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class ProjectDiscoveryProductCommandHandler implements CommandHandler<ProjectDiscoveryProductCommand, Void> {

    private final ProjectDiscoveryProductUseCase useCase;

    public ProjectDiscoveryProductCommandHandler(ProjectDiscoveryProductUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @DiscoveryTransactional(propagation = Propagation.REQUIRES_NEW)
    public Void handle(ProjectDiscoveryProductCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<ProjectDiscoveryProductCommand> getCommandType() {
        return ProjectDiscoveryProductCommand.class;
    }
}

