package com.inventory.application.service;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.inventory.application.port.inbound.GetInventorySecurityManifestPublicationStatusUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetInventorySecurityManifestPublicationStatusService implements GetInventorySecurityManifestPublicationStatusUseCase {
    private final SecurityManifestPublicationQueryPort publication;

    @Override
    public PublicationStatus execute() {
        return publication.status("inventory");
    }
}
