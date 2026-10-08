package com.catalog.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.catalog.application.model.write.PublishCatalogSecurityManifestCommand;
import com.catalog.application.port.inbound.PublishCatalogSecurityManifestUseCase;
import com.catalog.application.security.CatalogSecurityManifest;

public class PublishCatalogSecurityManifestService implements PublishCatalogSecurityManifestUseCase {
    private static final Logger log = Loggers.getLogger(PublishCatalogSecurityManifestService.class);
    private final SecurityManifestPublicationPort publication;

    public PublishCatalogSecurityManifestService(SecurityManifestPublicationPort publication) {
        this.publication = publication;
    }

    @Override
    public void execute(PublishCatalogSecurityManifestCommand command) {
        var result = publication.enqueue(CatalogSecurityManifest.CURRENT);
        log.info("Security manifest publication module: catalog, outcome: {}", result);
    }
}
