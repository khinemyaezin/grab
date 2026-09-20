package com.catalog.application.port.inbound;

import com.catalog.application.query.CategoryNodeResult;
import com.catalog.application.query.GetCategoryTreeQuery;

public interface GetCategoryTreeUseCase {
    CategoryNodeResult execute(GetCategoryTreeQuery query);
}
