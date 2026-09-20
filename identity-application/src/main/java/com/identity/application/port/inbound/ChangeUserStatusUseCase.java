package com.identity.application.port.inbound;

import com.identity.application.model.write.ChangeUserStatusCommand;
import com.identity.application.model.write.UserProfileResult;

public interface ChangeUserStatusUseCase {
    UserProfileResult execute(ChangeUserStatusCommand command);
}
