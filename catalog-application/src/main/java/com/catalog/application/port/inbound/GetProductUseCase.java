package com.catalog.application.port.inbound;

import com.catalog.application.model.read.GetProductQuery;
import com.catalog.application.model.read.GetProductResult;

public interface GetProductUseCase {
    GetProductResult execute(GetProductQuery query);
}
