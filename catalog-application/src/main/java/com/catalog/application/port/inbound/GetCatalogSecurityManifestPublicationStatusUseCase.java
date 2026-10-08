package com.catalog.application.port.inbound;

import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;

public interface GetCatalogSecurityManifestPublicationStatusUseCase {
    PublicationStatus execute();
}
