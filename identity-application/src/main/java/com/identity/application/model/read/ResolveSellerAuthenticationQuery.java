package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record ResolveSellerAuthenticationQuery(Id userId, Id assignmentId, boolean selectSingleContext)
        implements Query<SellerAuthenticationResult> {}
