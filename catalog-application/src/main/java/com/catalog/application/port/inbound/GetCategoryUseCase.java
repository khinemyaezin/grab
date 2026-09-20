package com.catalog.application.port.inbound;

import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryQuery;

public interface GetCategoryUseCase {
    CategoryResult execute(GetCategoryQuery query);
}
