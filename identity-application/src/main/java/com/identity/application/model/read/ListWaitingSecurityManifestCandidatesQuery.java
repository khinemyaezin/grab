package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;
import java.util.List;

public record ListWaitingSecurityManifestCandidatesQuery(int limit) implements Query<List<WaitingSecurityManifestView>> {
    public ListWaitingSecurityManifestCandidatesQuery {
        if (limit < 1 || limit > 1000) {
            throw new IllegalArgumentException("Candidate batch limit must be between 1 and 1000");
        }
    }
}
