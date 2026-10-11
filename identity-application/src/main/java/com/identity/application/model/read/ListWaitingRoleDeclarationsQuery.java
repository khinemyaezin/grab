package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;

import java.util.List;

public record ListWaitingRoleDeclarationsQuery(int limit) implements Query<List<WaitingRoleDeclarationView>> {
    public ListWaitingRoleDeclarationsQuery {
        if (limit < 1 || limit > 1000) {
            throw new IllegalArgumentException("Role declaration batch limit must be between 1 and 1000");
        }
    }
}
