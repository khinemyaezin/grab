package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.catalog.application.model.write.PublishCatalogSecurityManifestCommand;
import com.catalog.application.port.inbound.PublishCatalogSecurityManifestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublishCatalogSecurityManifestCommandHandler
        implements CommandHandler<PublishCatalogSecurityManifestCommand, Void> {
    private final PublishCatalogSecurityManifestUseCase useCase;

    @Override
    @CatalogTransactional
    public Void handle(PublishCatalogSecurityManifestCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<PublishCatalogSecurityManifestCommand> getCommandType() {
        return PublishCatalogSecurityManifestCommand.class;
    }
}
