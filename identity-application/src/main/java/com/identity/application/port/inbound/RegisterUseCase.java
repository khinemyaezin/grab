package com.identity.application.port.inbound;

import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;

public interface RegisterUseCase {
    UserProfileResult execute(RegisterCommand command);
}
