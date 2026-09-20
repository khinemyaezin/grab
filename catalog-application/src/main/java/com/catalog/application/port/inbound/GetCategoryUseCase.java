package com.catalog.application.port.inbound;

import com.catalog.application.query.CategoryResult;
import com.catalog.application.query.GetCategoryQuery;

public interface GetCategoryUseCase {
    CategoryResult execute(GetCategoryQuery query);
}
