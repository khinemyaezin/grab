package com.catalog.application.service;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.catalog.application.port.inbound.GetCatalogSecurityManifestPublicationStatusUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetCatalogSecurityManifestPublicationStatusService implements GetCatalogSecurityManifestPublicationStatusUseCase {
    private final SecurityManifestPublicationQueryPort publication;

    @Override
    public PublicationStatus execute() {
        return publication.status("catalog");
    }
}
