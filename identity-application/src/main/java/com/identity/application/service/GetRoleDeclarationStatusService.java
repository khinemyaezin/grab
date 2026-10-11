package com.identity.application.service;

import com.identity.application.model.read.GetRoleDeclarationStatusQuery;
import com.identity.application.model.read.RoleDeclarationStatusView;
import com.identity.application.port.inbound.GetRoleDeclarationStatusUseCase;
import com.identity.application.port.outbound.RoleDeclarationQueryPort;

import java.util.Locale;

public class GetRoleDeclarationStatusService implements GetRoleDeclarationStatusUseCase {
    private final RoleDeclarationQueryPort declarations;

    public GetRoleDeclarationStatusService(RoleDeclarationQueryPort declarations) {
        this.declarations = declarations;
    }

    @Override
    public RoleDeclarationStatusView execute(GetRoleDeclarationStatusQuery query) {
        String roleCode = query.roleCode().trim().toUpperCase(Locale.ROOT);
        return declarations.status(roleCode);
    }
}
