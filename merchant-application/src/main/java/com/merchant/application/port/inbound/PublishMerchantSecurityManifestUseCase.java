package com.merchant.application.port.inbound;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;

public interface PublishMerchantSecurityManifestUseCase {
    PublicationResult execute(PublishMerchantSecurityManifestCommand command);
}
