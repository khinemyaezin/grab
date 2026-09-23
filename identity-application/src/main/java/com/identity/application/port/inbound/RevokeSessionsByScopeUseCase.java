package com.identity.application.port.inbound;

import com.identity.application.model.write.RevokeSessionsByScopeCommand;

public interface RevokeSessionsByScopeUseCase {
    Void execute(RevokeSessionsByScopeCommand command);
}
