package com.identity.application.port.inbound;

import com.identity.application.model.write.RegisterScopeManifestCommand;

public interface RegisterScopeManifestUseCase {
    void execute(RegisterScopeManifestCommand command);
}
