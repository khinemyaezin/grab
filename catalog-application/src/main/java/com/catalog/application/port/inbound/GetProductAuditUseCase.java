package com.catalog.application.port.inbound;

import com.catalog.application.query.GetProductAuditQuery;
import com.catalog.application.query.GetProductAuditResult;

public interface GetProductAuditUseCase {
    GetProductAuditResult execute(GetProductAuditQuery query);
}
