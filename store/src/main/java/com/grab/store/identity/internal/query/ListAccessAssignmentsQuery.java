package com.grab.store.identity.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.store.identity.internal.command.AccessAssignmentResult;

import java.util.List;

public record ListAccessAssignmentsQuery(
        Id userId,
        String actorScopeKey,
        String actorScopeId
) implements Query<List<AccessAssignmentResult>> {
}
