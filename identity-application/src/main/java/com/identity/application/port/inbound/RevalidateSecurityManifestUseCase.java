package com.identity.application.port.inbound;

import com.identity.application.model.write.RevalidateSecurityManifestCommand;
import com.identity.application.model.write.RegisterSecurityManifestResult;

public interface RevalidateSecurityManifestUseCase {
    RegisterSecurityManifestResult execute(RevalidateSecurityManifestCommand command);
}
