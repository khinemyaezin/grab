package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetRoleDeclarationStatusQuery(String roleCode) implements Query<RoleDeclarationStatusView> {
    public GetRoleDeclarationStatusQuery {
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalArgumentException("roleCode is required");
        }
    }
}
