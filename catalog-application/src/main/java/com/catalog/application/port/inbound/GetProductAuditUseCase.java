package com.catalog.application.port.inbound;

import com.catalog.application.model.read.GetProductAuditQuery;
import com.catalog.application.model.read.GetProductAuditResult;

public interface GetProductAuditUseCase {
    GetProductAuditResult execute(GetProductAuditQuery query);
}
