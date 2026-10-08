package com.merchant.application.service;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.merchant.application.port.inbound.GetMerchantSecurityManifestPublicationStatusUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetMerchantSecurityManifestPublicationStatusService implements GetMerchantSecurityManifestPublicationStatusUseCase {
    private final SecurityManifestPublicationQueryPort publication;

    @Override
    public PublicationStatus execute() {
        return publication.status("merchant");
    }
}
