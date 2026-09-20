package com.catalog.application.port.inbound;

import com.catalog.application.model.read.GetProductBySlugQuery;
import com.catalog.application.model.read.GetProductBySlugResult;

public interface GetProductBySlugUseCase {
    GetProductBySlugResult execute(GetProductBySlugQuery query);
}
