package com.identity.application.port.inbound;

import com.identity.application.model.write.RegisterSecurityManifestCommand;

public interface RegisterSecurityManifestUseCase {
    void execute(RegisterSecurityManifestCommand command);
}
