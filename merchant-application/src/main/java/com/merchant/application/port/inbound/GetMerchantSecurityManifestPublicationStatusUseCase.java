package com.merchant.application.port.inbound;

import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;

public interface GetMerchantSecurityManifestPublicationStatusUseCase {
    PublicationStatus execute();
}
