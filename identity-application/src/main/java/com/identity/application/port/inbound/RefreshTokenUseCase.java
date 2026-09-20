package com.identity.application.port.inbound;

import com.identity.application.model.write.RefreshTokenCommand;
import com.identity.application.model.write.AuthResult;

public interface RefreshTokenUseCase {
    AuthResult execute(RefreshTokenCommand command);
}
