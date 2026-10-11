package com.identity.application.service;

import com.identity.application.model.read.ListWaitingRoleDeclarationsQuery;
import com.identity.application.model.read.WaitingRoleDeclarationView;
import com.identity.application.port.inbound.ListWaitingRoleDeclarationsUseCase;
import com.identity.application.port.outbound.RoleDeclarationQueryPort;

import java.util.List;

public class ListWaitingRoleDeclarationsService implements ListWaitingRoleDeclarationsUseCase {
    private final RoleDeclarationQueryPort declarations;

    public ListWaitingRoleDeclarationsService(RoleDeclarationQueryPort declarations) {
        this.declarations = declarations;
    }

    @Override
    public List<WaitingRoleDeclarationView> execute(ListWaitingRoleDeclarationsQuery query) {
        return declarations.findWaiting(query.limit());
    }
}
