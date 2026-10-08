package com.saleschannel.application.service;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.saleschannel.application.port.inbound.GetSalesChannelSecurityManifestPublicationStatusUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetSalesChannelSecurityManifestPublicationStatusService implements GetSalesChannelSecurityManifestPublicationStatusUseCase {
    private final SecurityManifestPublicationQueryPort publication;

    @Override
    public PublicationStatus execute() {
        return publication.status("saleschannel");
    }
}
