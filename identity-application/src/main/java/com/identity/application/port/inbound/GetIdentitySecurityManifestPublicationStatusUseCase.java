package com.identity.application.port.inbound;

import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;

public interface GetIdentitySecurityManifestPublicationStatusUseCase {
    PublicationStatus execute();
}
