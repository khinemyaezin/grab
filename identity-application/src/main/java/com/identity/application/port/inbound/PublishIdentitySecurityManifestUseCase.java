package com.identity.application.port.inbound;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.identity.application.model.write.PublishIdentitySecurityManifestCommand;

public interface PublishIdentitySecurityManifestUseCase {
    PublicationResult execute(PublishIdentitySecurityManifestCommand command);
}
