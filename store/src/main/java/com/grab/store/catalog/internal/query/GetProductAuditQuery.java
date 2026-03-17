package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetProductAuditQuery(String productId) implements Query<GetProductAuditResult> {
}
