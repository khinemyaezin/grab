package com.catalog.application.port.inbound;

import com.catalog.application.query.GetProductQuery;
import com.catalog.application.query.GetProductResult;

public interface GetProductUseCase {
    GetProductResult execute(GetProductQuery query);
}
