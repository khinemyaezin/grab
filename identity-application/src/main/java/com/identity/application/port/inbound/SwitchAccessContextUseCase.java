package com.identity.application.port.inbound;

import com.identity.application.model.write.SwitchAccessContextCommand;
import com.identity.application.model.write.AuthResult;

public interface SwitchAccessContextUseCase {
    AuthResult execute(SwitchAccessContextCommand command);
}
