package com.inventory.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.inventory.application.model.write.PublishInventorySecurityManifestCommand;
import com.inventory.application.port.inbound.PublishInventorySecurityManifestUseCase;
import com.inventory.application.security.InventorySecurityManifest;

public class PublishInventorySecurityManifestService implements PublishInventorySecurityManifestUseCase {
    private static final Logger log = Loggers.getLogger(PublishInventorySecurityManifestService.class);
    private final SecurityManifestPublicationPort publication;

    public PublishInventorySecurityManifestService(SecurityManifestPublicationPort publication) {
        this.publication = publication;
    }

    @Override
    public void execute(PublishInventorySecurityManifestCommand command) {
        var result = publication.enqueue(InventorySecurityManifest.CURRENT);
        log.info("Security manifest publication module: inventory, outcome: {}", result);
    }
}
