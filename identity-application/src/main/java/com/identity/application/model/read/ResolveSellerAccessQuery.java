package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record ResolveSellerAccessQuery(String issuer, String subject, Id assignmentId, Intent intent)
        implements Query<SellerAccessResult> {
    public enum Intent { INITIAL, REVALIDATE, SELECT }
}
