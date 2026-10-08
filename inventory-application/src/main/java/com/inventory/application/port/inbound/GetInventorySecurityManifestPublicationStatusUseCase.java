package com.inventory.application.port.inbound;

import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;

public interface GetInventorySecurityManifestPublicationStatusUseCase {
    PublicationStatus execute();
}
