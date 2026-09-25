package com.identity.application.port.inbound;

import com.identity.application.model.write.RegisterAuthorityManifestCommand;

public interface RegisterAuthorityManifestUseCase {
    void execute(RegisterAuthorityManifestCommand command);
}
