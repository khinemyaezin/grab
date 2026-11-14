package com.catalog.application.port.inbound;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.catalog.application.model.write.PublishCatalogSecurityManifestCommand;

public interface PublishCatalogSecurityManifestUseCase {
    PublicationResult execute(PublishCatalogSecurityManifestCommand command);
}
