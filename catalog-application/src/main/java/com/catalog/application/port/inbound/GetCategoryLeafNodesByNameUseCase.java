package com.catalog.application.port.inbound;

import com.catalog.application.query.CategoryLeavesResult;
import com.catalog.application.query.GetCategoryLeafNodesByNameQuery;

public interface GetCategoryLeafNodesByNameUseCase {
    CategoryLeavesResult execute(GetCategoryLeafNodesByNameQuery query);
}
