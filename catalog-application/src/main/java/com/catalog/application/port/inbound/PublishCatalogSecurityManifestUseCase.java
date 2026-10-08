package com.catalog.application.port.inbound;

import com.catalog.application.model.write.PublishCatalogSecurityManifestCommand;

public interface PublishCatalogSecurityManifestUseCase {
    void execute(PublishCatalogSecurityManifestCommand command);
}
