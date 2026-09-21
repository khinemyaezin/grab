package com.identity.application.port.inbound;

import com.identity.application.model.write.LogoutCommand;

public interface LogoutUseCase {
    Void execute(LogoutCommand command);
}
