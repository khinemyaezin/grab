package com.catalog.application.port.inbound;

import com.catalog.application.query.CategoryChildrenResult;
import com.catalog.application.query.GetCategoryChildrenQuery;

public interface GetCategoryChildrenUseCase {
    CategoryChildrenResult execute(GetCategoryChildrenQuery query);
}
