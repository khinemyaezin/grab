package com.catalog.application.port.inbound;

import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryParentQuery;

public interface GetCategoryParentUseCase {
    CategoryResult execute(GetCategoryParentQuery query);
}
