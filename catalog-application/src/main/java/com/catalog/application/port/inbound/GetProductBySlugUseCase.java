package com.catalog.application.port.inbound;

import com.catalog.application.query.GetProductBySlugQuery;
import com.catalog.application.query.GetProductBySlugResult;

public interface GetProductBySlugUseCase {
    GetProductBySlugResult execute(GetProductBySlugQuery query);
}
