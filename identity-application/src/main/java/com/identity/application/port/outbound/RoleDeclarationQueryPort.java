package com.identity.application.port.outbound;

import com.identity.application.model.read.RoleDeclarationStatusView;
import com.identity.application.model.read.WaitingRoleDeclarationView;

import java.util.List;

public interface RoleDeclarationQueryPort {
    List<WaitingRoleDeclarationView> findWaiting(int limit);

    RoleDeclarationStatusView status(String roleCode);
}
