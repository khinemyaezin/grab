package com.merchant.application.port.inbound;

import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;

public interface PublishMerchantSecurityManifestUseCase {
    void execute(PublishMerchantSecurityManifestCommand command);
}
