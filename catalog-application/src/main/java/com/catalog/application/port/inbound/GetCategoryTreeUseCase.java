package com.catalog.application.port.inbound;

import com.catalog.application.model.read.CategoryNodeResult;
import com.catalog.application.model.read.GetCategoryTreeQuery;

public interface GetCategoryTreeUseCase {
    CategoryNodeResult execute(GetCategoryTreeQuery query);
}
