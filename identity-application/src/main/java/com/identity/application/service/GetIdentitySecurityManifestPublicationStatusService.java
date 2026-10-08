package com.identity.application.service;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.identity.application.port.inbound.GetIdentitySecurityManifestPublicationStatusUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetIdentitySecurityManifestPublicationStatusService implements GetIdentitySecurityManifestPublicationStatusUseCase {
    private final SecurityManifestPublicationQueryPort publication;

    @Override
    public PublicationStatus execute() {
        return publication.status("identity");
    }
}
