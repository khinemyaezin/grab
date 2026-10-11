package com.identity.application.port.inbound;

import com.identity.application.model.read.ListWaitingRoleDeclarationsQuery;
import com.identity.application.model.read.WaitingRoleDeclarationView;

import java.util.List;

public interface ListWaitingRoleDeclarationsUseCase {
    List<WaitingRoleDeclarationView> execute(ListWaitingRoleDeclarationsQuery query);
}
