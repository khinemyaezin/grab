package com.identity.application.port.inbound;

import com.identity.application.model.write.RegisterSecurityManifestCommand;
import com.identity.application.model.write.RegisterSecurityManifestResult;

public interface RegisterSecurityManifestUseCase {
    RegisterSecurityManifestResult execute(RegisterSecurityManifestCommand command);
}
