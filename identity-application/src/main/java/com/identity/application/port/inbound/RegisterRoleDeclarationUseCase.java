package com.identity.application.port.inbound;

import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import com.identity.application.model.write.RegisterRoleDeclarationResult;

public interface RegisterRoleDeclarationUseCase {
    RegisterRoleDeclarationResult execute(RegisterRoleDeclarationCommand command);
}
