package com.identity.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.identity.application.model.write.PublishIdentitySecurityManifestCommand;
import com.identity.application.port.inbound.PublishIdentitySecurityManifestUseCase;
import com.identity.application.security.IdentitySecurityManifest;

public class PublishIdentitySecurityManifestService implements PublishIdentitySecurityManifestUseCase {
    private static final Logger log = Loggers.getLogger(PublishIdentitySecurityManifestService.class);
    private final SecurityManifestPublicationPort publication;

    public PublishIdentitySecurityManifestService(SecurityManifestPublicationPort publication) {
        this.publication = publication;
    }

    @Override
    public void execute(PublishIdentitySecurityManifestCommand command) {
        var result = publication.enqueue(IdentitySecurityManifest.CURRENT);
        log.info("Security manifest publication module: identity, outcome: {}", result);
    }
}
