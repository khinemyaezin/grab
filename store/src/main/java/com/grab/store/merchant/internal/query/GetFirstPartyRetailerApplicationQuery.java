package com.grab.store.merchant.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record GetFirstPartyRetailerApplicationQuery(Id applicantUserId) implements Query<GetFirstPartyRetailerApplicationResult> {
}
