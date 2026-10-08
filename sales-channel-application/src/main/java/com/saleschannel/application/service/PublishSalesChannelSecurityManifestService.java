package com.saleschannel.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.saleschannel.application.model.write.PublishSalesChannelSecurityManifestCommand;
import com.saleschannel.application.port.inbound.PublishSalesChannelSecurityManifestUseCase;
import com.saleschannel.application.security.SalesChannelSecurityManifest;

public class PublishSalesChannelSecurityManifestService implements PublishSalesChannelSecurityManifestUseCase {
    private static final Logger log = Loggers.getLogger(PublishSalesChannelSecurityManifestService.class);
    private final SecurityManifestPublicationPort publication;

    public PublishSalesChannelSecurityManifestService(SecurityManifestPublicationPort publication) {
        this.publication = publication;
    }

    @Override
    public void execute(PublishSalesChannelSecurityManifestCommand command) {
        var result = publication.enqueue(SalesChannelSecurityManifest.CURRENT);
        log.info("Security manifest publication module: saleschannel, outcome: {}", result);
    }
}
