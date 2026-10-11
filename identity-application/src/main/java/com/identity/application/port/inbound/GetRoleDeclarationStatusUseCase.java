package com.identity.application.port.inbound;

import com.identity.application.model.read.GetRoleDeclarationStatusQuery;
import com.identity.application.model.read.RoleDeclarationStatusView;

public interface GetRoleDeclarationStatusUseCase {
    RoleDeclarationStatusView execute(GetRoleDeclarationStatusQuery query);
}
