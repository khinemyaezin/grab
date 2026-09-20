package com.catalog.application.port.inbound;

import com.catalog.application.model.read.CategoryChildrenResult;
import com.catalog.application.model.read.GetCategoryChildrenQuery;

public interface GetCategoryChildrenUseCase {
    CategoryChildrenResult execute(GetCategoryChildrenQuery query);
}
