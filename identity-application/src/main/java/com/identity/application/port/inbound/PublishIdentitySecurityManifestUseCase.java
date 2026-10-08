package com.identity.application.port.inbound;

import com.identity.application.model.write.PublishIdentitySecurityManifestCommand;

public interface PublishIdentitySecurityManifestUseCase {
    void execute(PublishIdentitySecurityManifestCommand command);
}
