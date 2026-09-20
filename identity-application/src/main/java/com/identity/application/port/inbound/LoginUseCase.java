package com.identity.application.port.inbound;

import com.identity.application.model.write.LoginCommand;
import com.identity.application.model.write.AuthResult;

public interface LoginUseCase {
    AuthResult execute(LoginCommand command);
}
