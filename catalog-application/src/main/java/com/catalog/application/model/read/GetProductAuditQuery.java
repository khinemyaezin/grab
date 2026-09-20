package com.catalog.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetProductAuditQuery(String merchantId, String productId) implements Query<GetProductAuditResult> {
}
