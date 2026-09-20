package com.catalog.application.port.inbound;

import com.catalog.application.query.CategoryResult;
import com.catalog.application.query.GetCategoryParentQuery;

public interface GetCategoryParentUseCase {
    CategoryResult execute(GetCategoryParentQuery query);
}
