package com.grab.service.merchandising.catalog.internal.command.handler;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.catalog.application.model.write.PublishCatalogSecurityManifestCommand;
import com.catalog.application.port.inbound.PublishCatalogSecurityManifestUseCase;
import com.grab.service.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.service.merchandising.shared.security.SecurityManifestPublicationRetryable;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class PublishCatalogSecurityManifestCommandHandler
        implements CommandHandler<PublishCatalogSecurityManifestCommand, PublicationResult> {

    private final PublishCatalogSecurityManifestUseCase useCase;

    public PublishCatalogSecurityManifestCommandHandler(PublishCatalogSecurityManifestUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SecurityManifestPublicationRetryable
    @CatalogTransactional(propagation = Propagation.REQUIRES_NEW)
    public PublicationResult handle(PublishCatalogSecurityManifestCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<PublishCatalogSecurityManifestCommand> getCommandType() {
        return PublishCatalogSecurityManifestCommand.class;
    }
}

