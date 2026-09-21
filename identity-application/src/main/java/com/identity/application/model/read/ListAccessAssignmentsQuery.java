package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.identity.application.model.write.AccessAssignmentResult;

import java.util.List;

public record ListAccessAssignmentsQuery(
        Id userId,
        String actorScopeKey,
        String actorScopeId
) implements Query<List<AccessAssignmentResult>> {
}
