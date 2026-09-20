package com.identity.application.port.inbound;

import com.identity.application.model.write.CreateRoleCommand;
import com.identity.application.model.write.RoleResult;

public interface CreateRoleUseCase {
    RoleResult execute(CreateRoleCommand command);
}
