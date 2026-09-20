package com.identity.application.port.inbound;

import com.identity.application.model.write.ManageAuthorityCommand;
import com.identity.application.model.write.RoleResult;

public interface ManageAuthorityUseCase {
    RoleResult execute(ManageAuthorityCommand command);
}
