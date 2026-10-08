package com.merchant.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;
import com.merchant.application.port.inbound.PublishMerchantSecurityManifestUseCase;
import com.merchant.application.security.MerchantSecurityManifest;

public class PublishMerchantSecurityManifestService implements PublishMerchantSecurityManifestUseCase {
    private static final Logger log = Loggers.getLogger(PublishMerchantSecurityManifestService.class);
    private final SecurityManifestPublicationPort publication;

    public PublishMerchantSecurityManifestService(SecurityManifestPublicationPort publication) {
        this.publication = publication;
    }

    @Override
    public void execute(PublishMerchantSecurityManifestCommand command) {
        var result = publication.enqueue(MerchantSecurityManifest.CURRENT);
        log.info("Security manifest publication module: merchant, outcome: {}", result);
    }
}
