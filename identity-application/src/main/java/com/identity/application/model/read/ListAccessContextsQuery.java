package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.List;

public record ListAccessContextsQuery(
        Id userId,
        String platformCode
) implements Query<List<AccessContextResult>> {
}
